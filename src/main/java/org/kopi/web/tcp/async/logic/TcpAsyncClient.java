package org.kopi.web.tcp.async.logic;

import org.kopi.web.socket.itf.SocketClient;

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
