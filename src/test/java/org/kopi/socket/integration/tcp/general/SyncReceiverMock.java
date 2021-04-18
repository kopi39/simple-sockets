package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.integration.tcp.config.IntegrationTestsConfig;
import org.kopi.socket.tcp.strategies.sync.itf.SyncReceiver;
import org.kopi.util.encoding.itf.EncodingService;

public class SyncReceiverMock implements SyncReceiver {

    private final EncodingService<String, byte[]> encodingService;

    public SyncReceiverMock(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public Response process(byte[] input) {
        String message = this.encodingService.decode(input);
        if (IntegrationTestsConfig.CLOSE_SIGNAL.equals(message)) {
            return Response.closeServer();
        }
        String responseMessage = "server -> " + message;
        byte[] encodedResponseMessage = this.encodingService.encode(responseMessage);
        return Response.create(encodedResponseMessage);
    }
}
