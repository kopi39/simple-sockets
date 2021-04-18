package org.kopi.socket.tcp.strategies.mix;

import org.kopi.util.security.itf.EncryptionService;
import org.kopi.socket.tcp.strategies.sync.SyncStrategy;
import org.kopi.socket.tcp.strategies.sync.TcpSyncClient;

import java.io.OutputStream;
import java.net.Socket;

public class TcpMixSyncClient extends TcpSyncClient {

    public TcpMixSyncClient(EncryptionService encryptionService) {
        super(encryptionService);
    }

    @Override
    protected void onConnect(Socket clientSocket) throws Exception {
        super.onConnect(clientSocket);
        OutputStream out = clientSocket.getOutputStream();
        out.write(SyncStrategy.CODE);
        out.flush();
    }
}
