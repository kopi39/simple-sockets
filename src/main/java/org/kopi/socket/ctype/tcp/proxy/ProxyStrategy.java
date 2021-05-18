package org.kopi.socket.ctype.tcp.proxy;

import org.kopi.socket.ctype.tcp.proxy.itf.Interceptor;
import org.kopi.socket.general.ex.SimpleSocketException;
import org.kopi.socket.itf.BytesReader;
import org.kopi.socket.itf.BytesWriter;
import org.kopi.socket.itf.OnConnect;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.util.async.Async;
import org.kopi.socket.util.io.SafeClose;
import org.kopi.socket.util.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyStrategy implements SocketStrategy {

    public static final int CODE = 4;
    private static final Logger LOG = Log.get();
    private final List<PipeElem> toServerPipe = new ArrayList<>();
    private final List<PipeElem> toClientPipe = new ArrayList<>();
    private final String host;
    private final int port;
    private final ProxyType type;
    private final BytesReader reader;
    private final BytesWriter writer;
    private final BytesReader outReader;
    private final BytesWriter outWriter;
    private final OnConnect onConnect;

    private Socket client;
    private OutputStream clientIn;
    private InputStream clientOut;

    private Socket server;
    private OutputStream serverIn;
    private InputStream serverOut;

    public ProxyStrategy(String host, int port, BytesReader reader, BytesWriter writer, Interceptor... interceptors) {
        this(host, port, ProxyType.PASS_DATA, reader, writer, x -> {
        }, null, null, interceptors);
    }

    public ProxyStrategy(
            String host,
            int port,
            ProxyType type,
            BytesReader reader,
            BytesWriter writer,
            OnConnect onConnect,
            BytesReader outReader,
            BytesWriter outWriter,
            Interceptor... interceptors) {
        this.host = host;
        this.port = port;
        this.type = type;
        this.reader = reader;
        this.writer = writer;
        this.onConnect = onConnect;
        this.outReader = outReader;
        this.outWriter = outWriter;
        this.createPipes(interceptors);
    }

    @Override
    public Result apply(Socket client) {
        if (!attachTo(client, host, port)) {
            close();
            return Result.disconnectClient();
        }

        Thread clientToServer = Async.start(this::passDataFromClientToServer);
        Thread serverToClient = Async.start(this::passDataFromServerToClient);
        Async.stopAllWhenFirstEnds(1, this::close, clientToServer, serverToClient);

        return Result.disconnectClient();
    }

    @Override
    public int getStrategyCode() {
        return CODE;
    }

    @Override
    public void close() {
        SafeClose.close(clientIn, clientOut, serverIn, serverOut, client, server);
    }

    private boolean attachTo(Socket client, String host, int port) {
        try {
            this.client = client;
            this.clientIn = client.getOutputStream();
            this.clientOut = client.getInputStream();

            this.server = new Socket(host, port);
            this.onConnect.invoke(this.server);
            this.serverIn = server.getOutputStream();
            this.serverOut = server.getInputStream();

            return true;
        } catch (IOException | SimpleSocketException ex) {
            String message = String.format("Proxy server can't connect to %s:%d", host, port);
            LOG.log(Level.SEVERE, message, ex);
        }
        return false;
    }

    private void readNormalPassDynamic(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        while (true) {
            byte[] received = outReader.read(out).orElse(null);
            if (received == null) {
                break;
            }
            byte[] intercepted = applyPipe(received, pipe).orElse(null);
            writer.write(in, intercepted);
        }
    }

    private void readNormalPassNormal(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        while (true) {
            byte[] received = reader.read(out).orElse(null);
            if (received == null) {
                break;
            }
            byte[] intercepted = applyPipe(received, pipe).orElse(null);
            writer.write(in, intercepted);
        }
    }

    private void readDynamicPassNormal(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        while (true) {
            byte[] body = reader.read(out).orElse(null);
            if (body == null) {
                return;
            }
            byte[] intercepted = applyPipe(body, pipe).orElse(null);
            outWriter.write(in, intercepted);
        }
    }

    private Optional<byte[]> applyPipe(byte[] data, List<PipeElem> pipe) {
        byte[] result = data;
        for (PipeElem pipeElem : pipe) {
            result = pipeElem.apply(result).orElse(null);
            if (result == null) {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(result);
    }

    private void passDataFromServerToClient() {
        try {
            switch (type) {
                case SERVER:
                    readNormalPassDynamic(serverOut, clientIn, toClientPipe);
                    break;
                case CLIENT:
                    readDynamicPassNormal(serverOut, clientIn, toClientPipe);
                    break;
                default:
                    readNormalPassNormal(serverOut, clientIn, toClientPipe);
            }
        } catch (IOException | SimpleSocketException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void passDataFromClientToServer() {
        try {
            switch (type) {
                case SERVER:
                    readDynamicPassNormal(clientOut, serverIn, toServerPipe);
                    break;
                case CLIENT:
                    readNormalPassDynamic(clientOut, serverIn, toServerPipe);
                default:
                    readNormalPassNormal(clientOut, serverIn, toServerPipe);
            }
        } catch (IOException | SimpleSocketException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    private void createPipes(Interceptor[] interceptorsArray) {
        List<Interceptor> interceptors = new ArrayList<>(Arrays.asList(interceptorsArray));
        interceptors.stream()
                .sorted(Comparator.comparingInt(Interceptor::toClientIndex))
                .forEach(x -> this.toClientPipe.add(x::toClient));
        interceptors.stream()
                .sorted(Comparator.comparingInt(Interceptor::toServerIndex))
                .forEach(x -> this.toServerPipe.add(x::toServer));
    }

    public enum ProxyType {
        SERVER, CLIENT, PASS_DATA
    }

    private interface PipeElem {
        Optional<byte[]> apply(byte[] data);
    }
}
