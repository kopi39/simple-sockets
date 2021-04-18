package org.kopi.socket.examples.tcp.async;

import org.kopi.socket.examples.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.socket.tcp.general.AnyStrategySelector;
import org.kopi.socket.tcp.general.OneToOneSocketServer;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.itf.StrategySelector;
import org.kopi.socket.tcp.strategies.async.AsyncStrategy;
import org.kopi.socket.examples.tcp.general.ConsoleProducer;
import org.kopi.socket.examples.tcp.general.ConsoleReceiver;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);

        StrategySelector strategySelector = new AnyStrategySelector();
        SocketStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);

        try (SocketServer server = new OneToOneSocketServer(strategySelector, asyncStrategy)) {
            server.start(Config.PORT);
        }
    }
}
