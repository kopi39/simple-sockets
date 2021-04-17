package org.kopi.web.tcp.async.logic;

import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.async.logic.itf.Producer;
import org.kopi.web.tcp.async.logic.itf.Receiver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpAsyncServer extends AsyncTcpSocket {
    private static final int SERVER_BACKLOG = 1;

    private ServerSocket serverSocket;
    private final AtomicBoolean stopServer = new AtomicBoolean(false);

    public TcpAsyncServer(Producer producer, Receiver receiver, EncryptionService encryptionService) {
        super(producer, receiver, encryptionService);
    }

    @Override
    protected void onCloseSocketRequest() {
        this.stopServer();
    }

    public void start(int port) {
        initServer(port);
        while (!stopServer.get()) {
            listenForClient();
        }
    }

    public void stopServer() {
        this.stopServer.set(true);
        close();
    }

    private void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port, SERVER_BACKLOG);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void listenForClient() {
        try {
            Socket clientSocket = serverSocket.accept();
            super.start(clientSocket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        super.close();
        SafeClose.close(serverSocket);
    }
}
