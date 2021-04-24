package org.kopi.socket.examples.tcp.sync;

import org.kopi.socket.ctype.tcp.sync.itf.SyncReceiver;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.SimpleServerSyncReceiver;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.security.AesEncryptionService;
import org.kopi.socket.util.security.itf.EncryptionService;

import java.util.function.Supplier;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        Supplier<SyncReceiver> syncReceiver = () -> new SimpleServerSyncReceiver(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketServer server = socketFactory.createSyncServer(syncReceiver)) {
            server.start(Config.PORT);
        }
    }

}
