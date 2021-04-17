package org.kopi.web.tcp.sync.logic;

import org.kopi.config.Config;
import org.kopi.util.encoding.itf.EncodingService;

public class SimpleServerInterpreter implements TcpSyncServer.Interpreter {

    private final EncodingService<String, byte[]> encodingService;

    public SimpleServerInterpreter(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public TcpSyncServer.Response process(byte[] input) {
        String message = this.encodingService.decode(input);
        if (Config.CLOSE_SIGNAL.equals(message)) {
            return TcpSyncServer.Response.closeConnection();
        }
        System.out.println(message);
        String responseMessage = "server -> " + message;
        byte[] encodedResponseMessage = this.encodingService.encode(responseMessage);
        return TcpSyncServer.Response.create(encodedResponseMessage);
    }
}
