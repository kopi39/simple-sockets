package org.kopi.socket.ctype.tcp.sync;

import org.kopi.socket.ctype.tcp.sync.itf.SyncReceiver;
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

public class ReceiverSyncStrategy implements SocketStrategy {

    public static final int CODE = 1;
    private static final Logger LOG = Log.get();
    private final EncryptionService encryptionService;
    private final SyncReceiver syncReceiver;
    private final BytesReader reader;
    private final BytesWriter writer;

    private Socket clientSocket;
    private OutputStream out;
    private InputStream in;

    public ReceiverSyncStrategy(SyncReceiver syncReceiver, BytesReader reader, BytesWriter writer, EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        this.syncReceiver = syncReceiver;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public Result apply(Socket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            return startInternal();
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
        SafeClose.close(in, out, clientSocket);
    }

    private Result startInternal() throws IOException {
        out = clientSocket.getOutputStream();
        in = clientSocket.getInputStream();

        while (true) {
            byte[] input = reader.read(in).orElse(null);
            if (input == null) {
                break;
            }
            byte[] decryptedInput = this.encryptionService.decrypt(input);
            SyncReceiver.Response response = syncReceiver.process(decryptedInput);
            if (response.isCloseServer()) {
                return Result.stopServer();
            }
            byte[] encryptedOutput = this.encryptionService.encrypt(response.getBody());
            writer.write(out, encryptedOutput);
        }

        return Result.disconnectClient();
    }
}
