package org.kopi.web.tcp.sync;

import org.kopi.config.Config;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.kopi.web.tcp.sync.logic.TcpSyncClient;

import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        TcpSyncClient client = new TcpSyncClient(encryptionService);
        client.startConnection(Config.HOST, Config.PORT);

        Scanner in = new Scanner(System.in);

        while (true) {
            String message = in.nextLine().trim();
            byte[] response = client.sendMessage(encodingService.encode(message));
            if(response == null) {
                break;
            }
            String decodedResponse = encodingService.decode(response);
            System.out.println(decodedResponse);
            if (Config.CLOSE_SIGNAL.equals(message)) {
                break;
            }
        }

        client.stopConnection();
    }

}
