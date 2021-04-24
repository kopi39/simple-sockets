package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.ctype.tcp.async.itf.Producer;
import org.kopi.socket.ctype.tcp.async.itf.Sender;
import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.util.async.Async;
import org.kopi.socket.util.encoding.itf.EncodingService;

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
            if (IntegrationTestsConfig.CLOSE_SIGNAL.equalsIgnoreCase(message)) {
                break;
            }
        }
        Async.sleep(100);
    }
}
