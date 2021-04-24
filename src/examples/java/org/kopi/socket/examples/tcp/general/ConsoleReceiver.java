package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.ctype.tcp.async.itf.Receiver;
import org.kopi.socket.examples.config.Config;
import org.kopi.socket.util.encoding.itf.EncodingService;

public class ConsoleReceiver implements Receiver {

    private final EncodingService<String, byte[]> encodingService;

    public ConsoleReceiver(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public boolean process(byte[] data) {
        String message = this.encodingService.decode(data);
        if (Config.CLOSE_SIGNAL.equals(message)) {
            return true;
        }
        System.out.println(message);
        return false;
    }
}
