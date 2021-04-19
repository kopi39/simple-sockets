package org.kopi.socket.tcp.proxy;

import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.async.Async;
import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;
import java.util.function.Function;

public class ProxyStrategy implements SocketStrategy {

    private static final int CODE = 4; // TODO ??

    private final Interceptor interceptor;
    private final String host;
    private final int port;

    private Socket client;
    private OutputStream clientIn;
    private ByteReader clientOut;

    private Socket server;
    private OutputStream serverIn;
    private ByteReader serverOut;

    public ProxyStrategy(String host, int port, Interceptor interceptor) {
        this.host = host;
        this.port = port;
        this.interceptor = interceptor;
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
            this.clientOut = new ByteReader(client.getInputStream());

            this.server = new Socket(host, port);
            this.serverIn = server.getOutputStream();
            this.serverOut = new ByteReader(server.getInputStream());

            return true;
        } catch (IOException ex) {
            System.out.printf("Proxy server can't connect to %s:%d - %s", host, port, ex.getMessage());
        }
        return false;
    }

    private void passData(ByteReader out, OutputStream in, Function<byte[], Optional<byte[]>> interceptor) throws IOException {
        while (true) {
            byte[] received = out.read().orElse(null);
            if (received == null) {
                break;
            }
            byte[] intercepted = interceptor.apply(received).orElse(null);
            if (intercepted != null) {
                in.write(intercepted);
                in.flush();
            }
        }
    }

    private void passDataFromServerToClient() throws IOException {
        passData(serverOut, clientIn, interceptor::toClient);
    }

    private void passDataFromClientToServer() throws IOException {
        passData(clientOut, serverIn, interceptor::toServer);
    }
}
