package org.kopi.socket.tcp.strategies.async;

import org.kopi.socket.itf.SocketClient;

import java.net.Socket;

public class TcpAsyncClient implements SocketClient {


    private final AsyncStrategy asyncStrategy;

    public TcpAsyncClient(AsyncStrategy asyncStrategy) {
        this.asyncStrategy = asyncStrategy;
    }

    @Override
    public void connect(String host, int port) {
        try {
            Socket clientSocket = new Socket(host, port);
            onConnect(clientSocket);
            this.asyncStrategy.apply(clientSocket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        this.asyncStrategy.close();
    }

    protected void onConnect(Socket clientSocket) throws Exception {

    }

}
