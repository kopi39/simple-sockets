package org.kopi.socket.examples.tcp.mix;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.ConsoleSyncProducer;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

public class ClientSync {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        SyncProducer syncProducer = new ConsoleSyncProducer(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketClient client = socketFactory.createSyncClient(syncProducer, true)) {
            client.connect(Config.HOST, Config.PORT);
        }
    }

}
