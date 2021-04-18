package org.kopi.web.tcp.mix.logic;

import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.sync.logic.SyncStrategy;
import org.kopi.web.tcp.sync.logic.TcpSyncClient;

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
