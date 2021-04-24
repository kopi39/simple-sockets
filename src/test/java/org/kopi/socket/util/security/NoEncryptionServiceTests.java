package org.kopi.socket.util.security;

import org.kopi.socket.util.security.itf.EncryptionService;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

public class NoEncryptionServiceTests {

    @DataProvider(name = "data")
    public Object[][] getData() {
        return new Object[][]{
                {"Some message".getBytes(StandardCharsets.UTF_8)},
                {"ANOTHER ONE".getBytes(StandardCharsets.UTF_8)},
                {null},
                {"&^$#(*()*/-+)_(*(".getBytes(StandardCharsets.UTF_8)}
        };
    }

    @Test(dataProvider = "data")
    public void encrypt_changesNothing_resultEqualsInput(byte[] input) {
        EncryptionService encryptionService = new NoEncryptionService();
        byte[] result = encryptionService.encrypt(input);
        Assert.assertEquals(input, result);
    }

    @Test(dataProvider = "data")
    public void decrypt_changesNothing_resultEqualsInput(byte[] input) {
        EncryptionService encryptionService = new NoEncryptionService();
        byte[] result = encryptionService.decrypt(input);
        Assert.assertEquals(input, result);
    }

}