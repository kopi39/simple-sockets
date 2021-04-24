package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.ctype.tcp.sync.itf.Sender;
import org.kopi.socket.ctype.tcp.sync.itf.SyncProducer;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.util.encoding.itf.EncodingService;

import java.util.Scanner;

public class ConsoleSyncProducer implements SyncProducer {

    private final EncodingService<String, byte[]> encodingService;

    public ConsoleSyncProducer(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public SocketStrategy.Result process(Sender sender) {
        Scanner in = new Scanner(System.in);

        while (true) {
            String message = in.nextLine().trim();
            byte[] encodedMessage = encodingService.encode(message);
            byte[] response = sender.send(encodedMessage);
            if (response == null) {
                break;
            }
            String decodedResponse = encodingService.decode(response);
            System.out.println(decodedResponse);
            if (Config.CLOSE_SIGNAL.equals(message)) {
                break;
            }
        }

        return SocketStrategy.Result.disconnectClient();
    }
}
