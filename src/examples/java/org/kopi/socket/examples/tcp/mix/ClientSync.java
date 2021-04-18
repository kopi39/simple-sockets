package org.kopi.socket.examples.tcp.mix;

import org.kopi.socket.examples.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.socket.itf.SyncSocketClient;
import org.kopi.socket.tcp.strategies.mix.TcpMixSyncClient;

import java.util.Scanner;

public class ClientSync {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);

        try (SyncSocketClient client = new TcpMixSyncClient(encryptionService)) {
            client.connect(Config.HOST, Config.PORT);
            Scanner in = new Scanner(System.in);

            while (true) {
                String message = in.nextLine().trim();
                byte[] response = client.send(encodingService.encode(message));
                if (response == null) {
                    break;
                }
                String decodedResponse = encodingService.decode(response);
                System.out.println(decodedResponse);
                if (Config.CLOSE_SIGNAL.equals(message)) {
                    break;
                }
            }
        }
    }
}
