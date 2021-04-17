package org.kopi.web.tcp.async.logic;

import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.async.logic.itf.Producer;
import org.kopi.web.tcp.async.logic.itf.Receiver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpAsyncServer extends AsyncTcpSocket {
    private ServerSocket serverSocket;

    public TcpAsyncServer(Producer producer, Receiver receiver, EncryptionService encryptionService) {
        super(producer, receiver, encryptionService);
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            super.start(clientSocket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                this.serverSocket.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
