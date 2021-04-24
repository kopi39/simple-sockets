package org.kopi.socket.ctype.tcp.proxy.interceptors;

import org.kopi.socket.ctype.tcp.proxy.itf.Interceptor;
import org.kopi.socket.util.security.itf.EncryptionService;

import java.util.Optional;

public class EncryptIncomingDecryptOutgoingInterceptor implements Interceptor {

    private final EncryptionService encryptionService;

    public EncryptIncomingDecryptOutgoingInterceptor(EncryptionService encryptionService) {
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

    @Override
    public int toServerIndex() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int toClientIndex() {
        return Integer.MAX_VALUE;
    }
}
