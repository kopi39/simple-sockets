package org.kopi.socket.examples.tcp.onetomany;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.SimpleServerSyncReceiver;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.strategies.sync.itf.SyncReceiver;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

import java.util.function.Supplier;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        Supplier<SyncReceiver> syncReceiver = () -> new SimpleServerSyncReceiver(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketServer server = socketFactory.createSyncServer(syncReceiver)) {
            server.start(Config.PORT, 3);
        }
    }

}