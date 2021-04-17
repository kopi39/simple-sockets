package org.kopi.web.tcp.async;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.web.socket.itf.SocketServer;
import org.kopi.web.tcp.async.logic.ConsoleProducer;
import org.kopi.web.tcp.async.logic.ConsoleReceiver;
import org.kopi.web.tcp.async.logic.TcpAsyncServer;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);

        try (SocketServer server = new TcpAsyncServer(producer, receiver, encryptionService)) {
            server.start(Config.PORT);
        }
    }
}
