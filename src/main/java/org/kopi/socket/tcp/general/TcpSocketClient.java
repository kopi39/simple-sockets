package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.util.io.SafeClose;

import java.net.Socket;

public class TcpSocketClient implements SocketClient {

    private final SocketStrategy strategy;
    private final OnConnect onConnect;

    private Socket clientSocket;

    public TcpSocketClient(SocketStrategy strategy, OnConnect onConnect) {
        this.strategy = strategy;
        this.onConnect = onConnect;
    }

    @Override
    public void connect(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            onConnect.invoke(clientSocket);
            strategy.apply(clientSocket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        this.strategy.close();
        SafeClose.close(clientSocket);
    }

    @FunctionalInterface
    public interface OnConnect {
        void invoke(Socket socket) throws Exception;
    }

}