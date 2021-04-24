package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.ctype.tcp.async.itf.Receiver;
import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.util.encoding.itf.EncodingService;

import java.util.ArrayList;
import java.util.List;

public class ReceiverMock implements Receiver {

    private final EncodingService<String, byte[]> encodingService;
    private final List<String> receivedMessages = new ArrayList<>();

    public ReceiverMock(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public boolean process(byte[] data) {
        String message = encodingService.decode(data);
        this.receivedMessages.add(message);
        return IntegrationTestsConfig.CLOSE_SIGNAL.equalsIgnoreCase(message);
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }
}
