package org.kopi.socket.tcp.proxy;

import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.async.Async;
import org.kopi.util.io.BytesUtil;
import org.kopi.util.io.SafeClose;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProxyStrategy implements SocketStrategy {

    private static final int CODE = 4;
    private static final int BUFF_SIZE = 1024;

    private final List<PipeElem> toServerPipe = new ArrayList<>();
    private final List<PipeElem> toClientPipe = new ArrayList<>();
    private final String host;
    private final int port;
    private final ProxyType type;

    private Socket client;
    private OutputStream clientIn;
    private InputStream clientOut;

    private Socket server;
    private OutputStream serverIn;
    private InputStream serverOut;

    public ProxyStrategy(String host, int port, Interceptor... interceptors) {
        this(host, port, ProxyType.PASS_DATA, interceptors);
    }

    public ProxyStrategy(String host, int port, ProxyType type, Interceptor... interceptors) {
        this.host = host;
        this.port = port;
        this.type = type;
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
            this.serverIn = server.getOutputStream();
            this.serverOut = server.getInputStream();

            return true;
        } catch (IOException ex) {
            System.out.printf("Proxy server can't connect to %s:%d - %s", host, port, ex.getMessage());
        }
        return false;
    }

    private void readNormalPassDynamic(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        byte[] buff = new byte[BUFF_SIZE];
        while (true) {
            byte[] received = BytesUtil.read(out, buff).orElse(null);
            if (received == null) {
                break;
            }
            byte[] intercepted = applyPipe(received, pipe).orElse(null);
            BytesUtil.writeDynamic(in, intercepted);
        }
    }

    private void readNormalPassNormal(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        byte[] buff = new byte[BUFF_SIZE];
        while (true) {
            byte[] received = BytesUtil.read(out, buff).orElse(null);
            if (received == null) {
                break;
            }
            byte[] intercepted = applyPipe(received, pipe).orElse(null);
            BytesUtil.write(in, intercepted);
        }
    }

    private void readDynamicPassNormal(InputStream out, OutputStream in, List<PipeElem> pipe) throws IOException {
        while (true) {
            byte[] body = BytesUtil.readDynamic(out).orElse(null);
            if (body == null) {
                return;
            }
            byte[] intercepted = applyPipe(body, pipe).orElse(null);
            BytesUtil.write(in, intercepted);
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

    private void passDataFromServerToClient() throws IOException {
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
    }

    private void passDataFromClientToServer() throws IOException {
        switch (type) {
            case SERVER:
                readDynamicPassNormal(clientOut, serverIn, toServerPipe);
                break;
            case CLIENT:
                readNormalPassDynamic(clientOut, serverIn, toServerPipe);
            default:
                readNormalPassNormal(clientOut, serverIn, toServerPipe);
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

    private interface PipeElem {
        Optional<byte[]> apply(byte[] data);
    }

    public enum ProxyType {
        SERVER, CLIENT, PASS_DATA;
    }
}
