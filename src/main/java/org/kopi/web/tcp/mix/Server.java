package org.kopi.web.tcp.mix;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.web.socket.FirstByteStrategySelector;
import org.kopi.web.socket.OneToOneSocketServer;
import org.kopi.web.socket.itf.SocketServer;
import org.kopi.web.socket.itf.StrategySelector;
import org.kopi.web.tcp.async.logic.AsyncStrategy;
import org.kopi.web.tcp.async.logic.ConsoleProducer;
import org.kopi.web.tcp.async.logic.ConsoleReceiver;
import org.kopi.web.tcp.sync.logic.SimpleServerInterpreter;
import org.kopi.web.tcp.sync.logic.SyncStrategy;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);
        SimpleServerInterpreter interpreter = new SimpleServerInterpreter(encodingService);

        StrategySelector strategySelector = new FirstByteStrategySelector();
        SyncStrategy syncStrategy = new SyncStrategy(interpreter, encryptionService);
        AsyncStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);

        try (SocketServer server = new OneToOneSocketServer(strategySelector, syncStrategy, asyncStrategy)) {
            server.start(Config.PORT);
        }
    }
}
