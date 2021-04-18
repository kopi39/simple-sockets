package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Sender;
import org.kopi.util.encoding.itf.EncodingService;

import java.util.List;

public class ProducerMock implements Producer {

    private final List<String> messagesToSend;
    private final EncodingService<String, byte[]> encodingService;

    public ProducerMock(List<String> messagesToSend, EncodingService<String, byte[]> encodingService) {
        this.messagesToSend = messagesToSend;
        this.encodingService = encodingService;
    }

    @Override
    public void process(Sender sender) {
        for (String message : messagesToSend) {
            byte[] encodedMessage = this.encodingService.encode(message);
            sender.send(encodedMessage);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // may be silent
            }
            if (IntegrationTestsConfig.CLOSE_SIGNAL.equalsIgnoreCase(message)) {
                break;
            }
        }
    }
}
