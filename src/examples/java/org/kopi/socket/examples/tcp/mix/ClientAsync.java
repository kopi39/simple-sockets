package org.kopi.socket.examples.tcp.mix;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.ConsoleProducer;
import org.kopi.socket.examples.tcp.general.ConsoleReceiver;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.security.AesEncryptionService;

public class ClientAsync {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        AesEncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        ConsoleProducer producer = new ConsoleProducer(encodingService);
        ConsoleReceiver receiver = new ConsoleReceiver(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

        try (SocketClient client = socketFactory.createAsyncClient(producer, receiver, true)) {
            client.connect(Config.HOST, Config.PORT);
        }
    }

}
