package org.kopi.socket.examples.tcp.general;

import org.kopi.socket.ctype.tcp.proxy.itf.Interceptor;
import org.kopi.socket.util.encoding.itf.EncodingService;

import java.util.Optional;

public class SimpleInterceptor implements Interceptor {

    private final EncodingService<String, byte[]> encodingService;

    public SimpleInterceptor(EncodingService<String, byte[]> encodingService) {
        this.encodingService = encodingService;
    }

    @Override
    public Optional<byte[]> toServer(byte[] fromClient) {
        String message = encodingService.decode(fromClient);
        System.out.println("TO SERVER: " + message);
        message = "Client: " + message;
        return Optional.of(encodingService.encode(message));
    }

    @Override
    public Optional<byte[]> toClient(byte[] fromServer) {
        String message = encodingService.decode(fromServer);
        System.out.println("TO CLIENT: " + message);
        message = "Server: " + message;
        return Optional.of(encodingService.encode(message));
    }
}
