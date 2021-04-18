package org.kopi.web.tcp.sync.logic;

import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.tcp.sync.logic.itf.Interpreter;
import org.kopi.web.tcp.sync.logic.model.Response;

import java.io.DataOutputStream;
import java.net.Socket;

public class SyncStrategy implements SocketStrategy {

    public static final int CODE = 1;

    private final EncryptionService encryptionService;
    private final Interpreter interpreter;

    public SyncStrategy(Interpreter interpreter, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.interpreter = interpreter;
    }

    private Socket clientSocket;
    private DataOutputStream out;
    private ByteReader in;

    @Override
    public Result apply(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new ByteReader(clientSocket.getInputStream());
            return startInternal();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getStrategyCode() {
        return CODE;
    }

    @Override
    public void close() {
        SafeClose.close(in, out, clientSocket);
    }

    private Result startInternal() throws Exception {
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new ByteReader(clientSocket.getInputStream());

        while (true) {
            byte[] input = in.read().orElse(null);
            if (input == null) {
                break;
            }
            byte[] decryptedInput = this.encryptionService.decrypt(input);
            Response response = interpreter.process(decryptedInput);
            if (response.isCloseServer()) {
                return Result.stopServer();
            }
            byte[] encryptedOutput = this.encryptionService.encrypt(response.getBody());
            out.write(encryptedOutput);
            out.flush();
        }

        return Result.disconnectClient();
    }
}
