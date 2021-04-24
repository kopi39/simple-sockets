package org.kopi.socket.util.security;

import org.kopi.socket.util.security.itf.EncryptionService;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryptionService implements EncryptionService {

    private static final String ALGORITHM = "AES";

    private final Cipher encryptor;
    private final Cipher decryptor;

    public AesEncryptionService(byte[] key) {
        SecretKeySpec secret = new SecretKeySpec(key, ALGORITHM);
        this.encryptor = createCipher(secret, Cipher.ENCRYPT_MODE, ALGORITHM);
        this.decryptor = createCipher(secret, Cipher.DECRYPT_MODE, ALGORITHM);
    }

    @Override
    public byte[] encrypt(byte[] data) {
        try {
            return encryptor.doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        try {
            return decryptor.doFinal(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Cipher createCipher(SecretKeySpec secret, int mode, String algorithm) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(mode, secret);
            return cipher;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
