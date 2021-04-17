package org.kopi.web.tcp.async.logic;

import org.kopi.util.async.Async;
import org.kopi.util.io.ByteReader;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.async.logic.itf.Producer;
import org.kopi.web.tcp.async.logic.itf.Receiver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public abstract class AsyncTcpSocket implements AutoCloseable {

    private static final int INTERVAL = 10; // 10 ms

    private final EncryptionService encryptionService;
    private final Producer producer;
    private final Receiver receiver;

    private Socket socket;
    private OutputStream out;
    private ByteReader in;

    public AsyncTcpSocket(Producer producer, Receiver receiver, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.producer = producer;
        this.receiver = receiver;
    }

    public void start(Socket socket) throws Exception {
        this.socket = socket;
        out = socket.getOutputStream();
        in = new ByteReader(socket.getInputStream());

        Thread receiverThread = Async.start(this::processInput);
        Thread producerThread = Async.start(() -> this.producer.process(this::sendMessage));
        Async.stopAllWhenFirstEnds(INTERVAL, this::closeInternal, receiverThread, producerThread);
    }

    private void sendMessage(byte[] data) {
        try {
            byte[] encryptedData = this.encryptionService.encrypt(data);
            out.write(encryptedData);
            out.flush();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void processInput() throws IOException {
        while (true) {
            byte[] input = in.read().orElse(null);
            if (input == null) {
                break;
            }
            byte[] decryptedInput = this.encryptionService.decrypt(input);
            boolean close = receiver.process(decryptedInput);
            if (close) {
                onCloseSocketRequest();
                break;
            }
        }
    }

    protected abstract void onCloseSocketRequest();

    private void closeInternal() {
        SafeClose.close(in, out, socket);
    }

    @Override
    public void close() {
        closeInternal();
    }

}
