package org.kopi.web.tcp.sync;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.sync.logic.SimpleServerInterpreter;
import org.kopi.web.tcp.sync.logic.TcpSyncServer;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SimpleServerInterpreter interpreter = new SimpleServerInterpreter(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        TcpSyncServer server = new TcpSyncServer(interpreter, encryptionService);
        server.start(Config.PORT);
    }

}
