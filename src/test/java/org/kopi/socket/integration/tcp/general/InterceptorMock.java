package org.kopi.socket.integration.tcp.general;

import org.kopi.socket.ctype.tcp.proxy.itf.Interceptor;
import org.kopi.socket.util.encoding.itf.EncodingService;

import java.util.Optional;

public class InterceptorMock implements Interceptor {

    private final EncodingService<String, byte[]> encodingService;

    private final int toServerIndex;
    private final int toClientIndex;
    private final String wrapWith;

    public InterceptorMock(int toServerIndex, int toClientIndex, String wrapWith, EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
        this.toServerIndex = toServerIndex;
        this.toClientIndex = toClientIndex;
        this.wrapWith = wrapWith;
    }

    @Override
    public Optional<byte[]> toServer(byte[] fromClient) {
        String message = encodingService.decode(fromClient);
        if (skipMessage(message)) {
            return Optional.empty();
        }
        String intercepted = wrap(message, wrapWith);
        return Optional.of(encodingService.encode(intercepted));
    }

    @Override
    public Optional<byte[]> toClient(byte[] fromServer) {
        String message = encodingService.decode(fromServer);
        if (skipMessage(message)) {
            return Optional.empty();
        }
        String intercepted = wrap(message, wrapWith);
        return Optional.of(encodingService.encode(intercepted));
    }

    @Override
    public int toServerIndex() {
        return this.toServerIndex;
    }

    @Override
    public int toClientIndex() {
        return this.toClientIndex;
    }

    private boolean skipMessage(String message) {
        return "X".equals(message);
    }

    private String wrap(String message, String wrapper) {
        return wrapper + message + wrapper;
    }

}
