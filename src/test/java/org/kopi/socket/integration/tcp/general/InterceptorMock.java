package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.encoding.itf.EncodingService;

import java.util.Optional;

public class InterceptorMock implements Interceptor {

    private final EncodingService<String, byte[]> encodingService;

    public InterceptorMock(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public Optional<byte[]> toServer(byte[] fromClient) {
        String message = encodingService.decode(fromClient);
        if (skipMessage(message)) {
            return Optional.empty();
        }
        String intercepted = wrap(message, "+");
        return Optional.of(encodingService.encode(intercepted));
    }

    @Override
    public Optional<byte[]> toClient(byte[] fromServer) {
        String message = encodingService.decode(fromServer);
        if (skipMessage(message)) {
            return Optional.empty();
        }
        String intercepted = wrap(message, "-");
        return Optional.of(encodingService.encode(intercepted));
    }

    private boolean skipMessage(String message) {
        return "X".equals(message);
    }


    private String wrap(String message, String wrapper) {
        return wrapper + message + wrapper;
    }

}
