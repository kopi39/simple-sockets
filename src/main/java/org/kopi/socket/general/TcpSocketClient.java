package org.kopi.socket.general;

import org.kopi.socket.general.ex.SimpleSocketException;
import org.kopi.socket.itf.OnConnect;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.util.io.SafeClose;

import java.io.IOException;
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
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }

    @Override
    public void close() {
        this.strategy.close();
        SafeClose.close(clientSocket);
    }

}
