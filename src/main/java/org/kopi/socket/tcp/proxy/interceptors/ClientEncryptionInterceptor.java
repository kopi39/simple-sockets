package org.kopi.socket.tcp.proxy.interceptors;

import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.security.itf.EncryptionService;

import java.util.Optional;

public class ClientEncryptionInterceptor implements Interceptor {

    private final EncryptionService encryptionService;

    public ClientEncryptionInterceptor(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public Optional<byte[]> toServer(byte[] fromClient) {
        byte[] encrypted = encryptionService.encrypt(fromClient);
        return Optional.of(encrypted);
    }

    @Override
    public Optional<byte[]> toClient(byte[] fromServer) {
        byte[] decrypted = encryptionService.decrypt(fromServer);
        return Optional.of(decrypted);
    }
}
