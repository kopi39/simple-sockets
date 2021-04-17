package org.kopi.web.tcp.async.logic;

import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.async.logic.itf.Producer;
import org.kopi.web.tcp.async.logic.itf.Receiver;

import java.net.Socket;

public class TcpAsyncClient extends AsyncTcpSocket {

    public TcpAsyncClient(Producer producer, Receiver receiver, EncryptionService encryptionService) {
        super(producer, receiver, encryptionService);
    }

    public void connect(String host, int port) {
        try {
            Socket clientSocket = new Socket(host, port);
            super.start(clientSocket);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void onCloseSocketRequest() {

    }

}
