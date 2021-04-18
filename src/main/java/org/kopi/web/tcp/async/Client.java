package org.kopi.web.tcp.async;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.web.socket.itf.SocketClient;
import org.kopi.web.tcp.async.logic.AsyncStrategy;
import org.kopi.web.tcp.async.logic.ConsoleProducer;
import org.kopi.web.tcp.async.logic.ConsoleReceiver;
import org.kopi.web.tcp.async.logic.TcpAsyncClient;

public class Client {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);

        AsyncStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);

        try (SocketClient client = new TcpAsyncClient(asyncStrategy)) {
            client.connect(Config.HOST, Config.PORT);
        }
    }

}
