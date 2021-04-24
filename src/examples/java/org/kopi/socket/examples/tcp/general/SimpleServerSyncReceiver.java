package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.ctype.tcp.sync.itf.SyncReceiver;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.util.encoding.itf.EncodingService;

public class SimpleServerSyncReceiver implements SyncReceiver {

    private final EncodingService<String, byte[]> encodingService;

    public SimpleServerSyncReceiver(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public Response process(byte[] input) {
        String message = this.encodingService.decode(input);
        if (Config.CLOSE_SIGNAL.equals(message)) {
            return Response.closeServer();
        }
        System.out.println(message);
        String responseMessage = "server -> " + message;
        byte[] encodedResponseMessage = this.encodingService.encode(responseMessage);
        return Response.create(encodedResponseMessage);
    }
}
