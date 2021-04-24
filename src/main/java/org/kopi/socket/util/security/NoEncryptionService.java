package org.kopi.socket.util.security;

import org.kopi.socket.util.security.itf.EncryptionService;

public class NoEncryptionService implements EncryptionService {

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] decrypt(byte[] data) {
        return data;
    }
}
