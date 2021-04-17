package org.kopi.web.tcp.sync.logic;

import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.socket.itf.SocketServer;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpSyncServer implements SocketServer {

    private static final int SERVER_BACKLOG = 1;

    private final EncryptionService encryptionService;
    private final Interpreter interpreter;
    private final AtomicBoolean stopServer = new AtomicBoolean(false);

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream out;
    private ByteReader in;

    public TcpSyncServer(Interpreter interpreter, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.interpreter = interpreter;
    }

    @Override
    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port, SERVER_BACKLOG);
            while (!this.stopServer.get()) {
                startInternal();
                closeInternal();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void stopServer() {
        stopServer.set(true);
        close();
    }

    private void startInternal() throws Exception {
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
            if (response.closeServer) {
                stopServer();
                break;
            }
            byte[] encryptedOutput = this.encryptionService.encrypt(response.body);
            out.write(encryptedOutput);
            out.flush();
        }
    }

    private void closeInternal() {
        SafeClose.close(in, out, clientSocket);
    }

    @Override
    public void close() {
        SafeClose.close(in, out, clientSocket, serverSocket);
    }

    @FunctionalInterface
    public interface Interpreter {
        Response process(byte[] input);
    }

    public static class Response {
        private boolean closeServer;
        private byte[] body;

        public static Response closeServer() {
            Response response = new Response();
            response.closeServer = true;
            return response;
        }

        public static Response create(byte[] body) {
            Response response = new Response();
            response.body = body;
            return response;
        }
    }

}
