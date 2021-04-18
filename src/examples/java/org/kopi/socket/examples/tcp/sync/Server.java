package org.kopi.socket.examples.tcp.sync;

import org.kopi.socket.examples.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.socket.tcp.general.AnyStrategySelector;
import org.kopi.socket.tcp.general.OneToOneSocketServer;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.itf.StrategySelector;
import org.kopi.socket.examples.tcp.general.SimpleServerInterpreter;
import org.kopi.socket.tcp.strategies.sync.SyncStrategy;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SimpleServerInterpreter interpreter = new SimpleServerInterpreter(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        StrategySelector strategySelector = new AnyStrategySelector();
        SocketStrategy syncStrategy = new SyncStrategy(interpreter, encryptionService);

        try (SocketServer server = new OneToOneSocketServer(strategySelector, syncStrategy)) {
            server.start(Config.PORT);
        }
    }

}
