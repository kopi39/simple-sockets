package org.kopi.socket.examples.tcp.sync;

import org.kopi.socket.ctype.tcp.sync.itf.SyncProducer;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.ConsoleSyncProducer;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.security.AesEncryptionService;
import org.kopi.socket.util.security.itf.EncryptionService;

public class Client {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        SyncProducer syncProducer = new ConsoleSyncProducer(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketClient client = socketFactory.createSyncClient(syncProducer, false)) {
            client.connect(Config.HOST, Config.PORT);
        }
    }

}
