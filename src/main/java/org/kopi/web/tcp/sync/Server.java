package org.kopi.web.tcp.sync;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.socket.AnyStrategySelector;
import org.kopi.web.socket.OneToOneSocketServer;
import org.kopi.web.socket.itf.SocketServer;
import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.socket.itf.StrategySelector;
import org.kopi.web.tcp.sync.logic.SimpleServerInterpreter;
import org.kopi.web.tcp.sync.logic.SyncStrategy;

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
