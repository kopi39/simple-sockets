package org.kopi.socket.tcp.strategies.async;

import org.kopi.socket.itf.BytesReader;
import org.kopi.socket.itf.BytesWriter;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Receiver;
import org.kopi.util.async.Async;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.itf.EncryptionService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class AsyncStrategy implements SocketStrategy {

    public static final int CODE = 0;

    private static final int INTERVAL = 1; // 1 ms

    private final EncryptionService encryptionService;
    private final Producer producer;
    private final Receiver receiver;
    private final BytesReader reader;
    private final BytesWriter writer;

    private Result result;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public AsyncStrategy(Producer producer, Receiver receiver, BytesReader reader, BytesWriter writer, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.producer = producer;
        this.receiver = receiver;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Result apply(Socket socket) {
        this.socket = socket;
        this.result = Result.disconnectClient();
        try {
            out = socket.getOutputStream();
            in = socket.getInputStream();
            Thread receiverThread = Async.start(this::processInput);
            Thread producerThread = Async.start(() -> this.producer.process(this::sendMessage));
            Async.stopAllWhenFirstEnds(INTERVAL, this::closeInternal, receiverThread, producerThread);
            return this.result;
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
        closeInternal();
    }

    private void sendMessage(byte[] data) {
        try {
            byte[] encryptedData = this.encryptionService.encrypt(data);
            writer.write(out, encryptedData);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void processInput() throws IOException {
        while (true) {
            byte[] input = reader.read(in).orElse(null);
            if (input == null) {
                break;
            }
            byte[] decryptedInput = this.encryptionService.decrypt(input);
            boolean close = receiver.process(decryptedInput);
            if (close) {
                this.result = Result.stopServer();
                return;
            }
        }
        this.result = Result.disconnectClient();
    }

    private void closeInternal() {
        SafeClose.close(in, out, socket);
    }
}
