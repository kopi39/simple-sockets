package org.kopi.web.tcp.sync;

import org.kopi.config.Config;
import org.kopi.web.tcp.sync.logic.TcpSyncClient;

import java.util.Scanner;

public class Client {

    public static void main(String[] args) throws Exception {
        TcpSyncClient client = new TcpSyncClient();
        client.startConnection(Config.HOST, Config.PORT);

        Scanner in = new Scanner(System.in);

        while (true) {
            String message = in.nextLine();
            String response = client.sendMessage(message);
            System.out.println(response);
            if (Config.CLOSE_SIGNAL.equals(message)) {
                break;
            }
        }

        client.stopConnection();
    }

}
