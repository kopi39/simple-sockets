package org.kopi.util.encoding;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Utf8EncodingServiceTests {

    @DataProvider(name = "data")
    public Object[][] getData() {
        return new Object[][]{
                {"Some message"},
                {"ANOTHER ONE"},
                {null},
                {"&^$#(*()*/-+)_(*("}
        };
    }

    @Test(dataProvider = "data")
    public void encode_thenDecode_resultEqualsInput(String input) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        byte[] encoded = encodingService.encode(input);
        String decoded = encodingService.decode(encoded);
        Assert.assertEquals(decoded, input);
    }

}