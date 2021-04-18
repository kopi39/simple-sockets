package org.kopi.web.tcp.sync.logic;

import org.kopi.config.Config;
import org.kopi.util.encoding.itf.EncodingService;
import org.kopi.web.tcp.sync.logic.itf.Interpreter;
import org.kopi.web.tcp.sync.logic.model.Response;

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
