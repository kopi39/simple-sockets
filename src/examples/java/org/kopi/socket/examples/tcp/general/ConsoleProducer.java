package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Sender;
import org.kopi.util.encoding.itf.EncodingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleProducer implements Producer {

    private final static int INTERVAL = 10;

    private final EncodingService<String, byte[]> encodingService;

    public ConsoleProducer(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public void process(Sender sender) {
        try {
            processInternal(sender);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public void processInternal(Sender sender) throws InterruptedException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            if (System.in.available() <= 0) {
                Thread.sleep(INTERVAL);
                continue;
            }
            String message = reader.readLine();
            byte[] encodedMessage = this.encodingService.encode(message);
            sender.send(encodedMessage);
            if (Config.CLOSE_SIGNAL.equals(message)) {
                break;
            }
        }
    }
}
