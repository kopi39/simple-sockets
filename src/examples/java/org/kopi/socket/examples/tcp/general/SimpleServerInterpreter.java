package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.tcp.strategies.sync.itf.Interpreter;
import org.kopi.util.encoding.itf.EncodingService;

public class SimpleServerInterpreter implements Interpreter {

    private final EncodingService<String, byte[]> encodingService;

    public SimpleServerInterpreter(EncodingService<String, byte[]> encodingService) {
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
