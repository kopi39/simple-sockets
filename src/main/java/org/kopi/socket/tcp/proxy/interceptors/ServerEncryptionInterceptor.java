package org.kopi.socket.tcp.proxy.interceptors;

import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.security.itf.EncryptionService;

import java.util.Optional;

public class ServerEncryptionInterceptor implements Interceptor {

    private final EncryptionService encryptionService;

    public ServerEncryptionInterceptor(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public Optional<byte[]> toServer(byte[] fromClient) {
        byte[] decrypted = encryptionService.decrypt(fromClient);
        return Optional.of(decrypted);
    }

    @Override
    public Optional<byte[]> toClient(byte[] fromServer) {
        byte[] encrypted = encryptionService.encrypt(fromServer);
        return Optional.of(encrypted);
    }
}
