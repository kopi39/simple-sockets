package org.kopi.util.security;

import org.kopi.util.security.itf.EncryptionService;

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
