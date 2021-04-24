package org.kopi.socket.util.security;

import org.kopi.socket.util.security.itf.EncryptionService;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class AesEncryptionServiceTests {

    private static final byte[] key = "4688fyu89DS(^&SA".getBytes();

    @DataProvider(name = "data")
    public Object[][] getData() {
        return new Object[][]{
                {"Some message".getBytes(StandardCharsets.UTF_8)},
                {"ANOTHER ONE".getBytes(StandardCharsets.UTF_8)},
                {"&^$#(*()*/-+)_(*(".getBytes(StandardCharsets.UTF_8)}
        };
    }

    @Test(dataProvider = "data")
    public void encrypt_thenDecrypt_resultEqualsInput(byte[] input) {
        EncryptionService encryptionService = new AesEncryptionService(key);
        byte[] encrypted = encryptionService.encrypt(input);
        byte[] decrypted = encryptionService.decrypt(encrypted);
        Assert.assertEquals(decrypted, input);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void encrypt_null_throwsException() {
        EncryptionService encryptionService = new AesEncryptionService(key);
        encryptionService.encrypt(null);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void decrypt_null_throwsException() {
        EncryptionService encryptionService = new AesEncryptionService(key);
        encryptionService.decrypt(null);
    }

}