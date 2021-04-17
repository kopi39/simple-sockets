package org.kopi.web.tcp.sync.logic;

import org.kopi.util.io.ByteReader;
import org.kopi.util.security.itf.EncryptionService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpSyncServer {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private ByteReader in;

    private final EncryptionService encryptionService;
    private final Interpreter interpreter;

    public TcpSyncServer(Interpreter interpreter, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.interpreter = interpreter;
    }

    public void start(int port) {
        try {
            startInternal(port);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void startInternal(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new ByteReader(clientSocket.getInputStream());

        while (true) {
            byte[] input = in.read().orElse(null);
            if (input == null) {
                break;
            }
            byte[] decryptedInput = this.encryptionService.decrypt(input);
            Response response = interpreter.process(decryptedInput);
            if (response.closeConnection) {
                break;
            }
            byte[] encryptedOutput = this.encryptionService.encrypt(response.body);
            out.write(encryptedOutput);
            out.flush();
        }

        stop();
    }

    private void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    @FunctionalInterface
    public interface Interpreter {
        Response process(byte[] input);
    }

    public static class Response {
        private boolean closeConnection;
        private byte[] body;

        public static Response closeConnection() {
            Response response = new Response();
            response.closeConnection = true;
            return response;
        }

        public static Response create(byte[] body) {
            Response response = new Response();
            response.body = body;
            return response;
        }
    }

}
