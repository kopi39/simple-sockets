package org.kopi.socket.examples.tcp.mix;

import org.kopi.socket.ctype.tcp.async.itf.Producer;
import org.kopi.socket.ctype.tcp.async.itf.Receiver;
import org.kopi.socket.ctype.tcp.sync.itf.SyncReceiver;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.ConsoleProducer;
import org.kopi.socket.examples.tcp.general.ConsoleReceiver;
import org.kopi.socket.examples.tcp.general.SimpleServerSyncReceiver;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.security.AesEncryptionService;

import java.util.function.Supplier;

public class Server {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        Supplier<Producer> producer = () -> new ConsoleProducer(encodingService);
        Supplier<Receiver> receiver = () -> new ConsoleReceiver(encodingService);
        Supplier<SyncReceiver> interpreter = () -> new SimpleServerSyncReceiver(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        TcpSocketFactory.Builder builder = socketFactory.createMixServer()
                .addSyncStrategy(interpreter)
                .addAsyncStrategy(producer, receiver);

        try (SocketServer server = builder.build()) {
            server.start(Config.PORT);
        }
    }
}
