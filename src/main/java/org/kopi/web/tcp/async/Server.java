package org.kopi.web.tcp.async;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.web.socket.AnyStrategySelector;
import org.kopi.web.socket.OneToOneSocketServer;
import org.kopi.web.socket.itf.SocketServer;
import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.socket.itf.StrategySelector;
import org.kopi.web.tcp.async.logic.AsyncStrategy;
import org.kopi.web.tcp.async.logic.ConsoleProducer;
import org.kopi.web.tcp.async.logic.ConsoleReceiver;

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
