package org.kopi.socket.tcp.strategies.sync;

import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.socket.itf.SyncSocketClient;

import java.io.OutputStream;
import java.net.Socket;

public class TcpSyncClient implements SyncSocketClient {

    private final EncryptionService encryptionService;

    private Socket clientSocket;
    private OutputStream out;
    private ByteReader in;

    public TcpSyncClient(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public void connect(String host, int port) {
        try {
            clientSocket = new Socket(host, port);
            onConnect(clientSocket);
            out = clientSocket.getOutputStream();
            in = new ByteReader(clientSocket.getInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte[] send(byte[] input) {
        try {
            byte[] encryptedInput = this.encryptionService.encrypt(input);
            out.write(encryptedInput);
            out.flush();
            byte[] response = in.read().orElse(null);
            if (response == null) {
                return null;
            }
            return this.encryptionService.decrypt(response);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        SafeClose.close(in, out, clientSocket);
    }

    protected void onConnect(Socket clientSocket) throws Exception {

    }

}
