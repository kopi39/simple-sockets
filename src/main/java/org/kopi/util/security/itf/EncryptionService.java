package org.kopi.util.security.itf;

public interface EncryptionService {

    byte[] encrypt(byte[] data);

    byte[] decrypt(byte[] data);
}
