package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.tcp.strategies.sync.itf.Sender;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.util.encoding.itf.EncodingService;

import java.util.ArrayList;
import java.util.List;

public class SyncProducerMock implements SyncProducer {

    public final EncodingService<String, byte[]> encodingService;
    private final List<String> messagesToSend;
    private final List<String> receivedMessages = new ArrayList<>();

    public SyncProducerMock(EncodingService<String, byte[]> encodingService, List<String> messagesToSend) {
        this.encodingService = encodingService;
        this.messagesToSend = messagesToSend;
    }

    @Override
    public SocketStrategy.Result process(Sender sender) {
        for (String message : messagesToSend) {
            byte[] encodedMessage = this.encodingService.encode(message);
            byte[] response = sender.send(encodedMessage);
            if (response == null) {
                break;
            }
            String decodedResponse = this.encodingService.decode(response);
            this.receivedMessages.add(decodedResponse);
        }
        return SocketStrategy.Result.disconnectClient();
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }
}
