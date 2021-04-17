package org.kopi.web.tcp.sync.logic;

import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class TcpSyncClient implements AutoCloseable {

    private final EncryptionService encryptionService;

    private Socket clientSocket;
    private OutputStream out;
    private ByteReader in;

    public TcpSyncClient(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public void startConnection(String host, int port) throws IOException {
        clientSocket = new Socket(host, port);
        out = clientSocket.getOutputStream();
        in = new ByteReader(clientSocket.getInputStream());
    }

    public byte[] sendMessage(byte[] input) {
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

}
