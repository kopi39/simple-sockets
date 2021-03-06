package org.kopi.socket.ctype.tcp.sync;

import org.kopi.socket.ctype.tcp.sync.itf.SyncProducer;
import org.kopi.socket.general.ex.SimpleSocketException;
import org.kopi.socket.itf.BytesReader;
import org.kopi.socket.itf.BytesWriter;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.util.io.SafeClose;
import org.kopi.socket.util.log.Log;
import org.kopi.socket.util.security.itf.EncryptionService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProducerSyncStrategy implements SocketStrategy {

    public static final int CODE = 2;
    private static final Logger LOG = Log.get();
    private final EncryptionService encryptionService;
    private final SyncProducer producer;
    private final BytesReader reader;
    private final BytesWriter writer;

    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public ProducerSyncStrategy(SyncProducer producer, BytesReader reader, BytesWriter writer, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.producer = producer;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Result apply(Socket clientSocket) {
        try {
            this.socket = clientSocket;
            this.out = clientSocket.getOutputStream();
            this.in = clientSocket.getInputStream();
            return producer.process(this::send);
        } catch (IOException | SimpleSocketException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return Result.disconnectClient();
        }
    }

    @Override
    public int getStrategyCode() {
        return CODE;
    }

    @Override
    public void close() {
        SafeClose.close(in, out, socket);
    }

    private byte[] send(byte[] input) {
        try {
            byte[] encryptedInput = this.encryptionService.encrypt(input);
            writer.write(out, encryptedInput);
            byte[] response = reader.read(in).orElse(null);
            if (response == null) {
                return null;
            }
            return this.encryptionService.decrypt(response);
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }
}
