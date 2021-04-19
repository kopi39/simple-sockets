package org.kopi.socket.examples.tcp.proxy.single;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.ConsoleProducer;
import org.kopi.socket.examples.tcp.general.ConsoleReceiver;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.NoEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

public class Client {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        EncryptionService encryptionService = new NoEncryptionService();
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketClient client = socketFactory.createAsyncClient(producer, receiver, false)) {
            client.connect(Config.HOST, Config.PROXY_PORT);
        }
    }
}
