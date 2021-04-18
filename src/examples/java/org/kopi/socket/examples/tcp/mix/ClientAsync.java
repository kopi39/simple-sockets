package org.kopi.socket.examples.tcp.mix;

import org.kopi.socket.examples.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.socket.tcp.strategies.async.AsyncStrategy;
import org.kopi.socket.examples.tcp.general.ConsoleProducer;
import org.kopi.socket.examples.tcp.general.ConsoleReceiver;
import org.kopi.socket.tcp.strategies.async.TcpAsyncClient;
import org.kopi.socket.tcp.strategies.mix.TcpMixAsyncClient;

public class ClientAsync {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);

        AsyncStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);

        try (TcpAsyncClient client = new TcpMixAsyncClient(asyncStrategy)) {
            client.connect(Config.HOST, Config.PORT);
        }
    }
}
