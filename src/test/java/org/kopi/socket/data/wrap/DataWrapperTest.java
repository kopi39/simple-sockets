package org.kopi.socket.data.wrap;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DataWrapperTest {

    @DataProvider(name = "data")
    public static Object[][] getDp() {
        return new Object[][]{
                {"header".getBytes(StandardCharsets.UTF_8), "body".getBytes(StandardCharsets.UTF_8)},
                {"#@!*#(_)#&^*@!^631287HK".getBytes(StandardCharsets.UTF_8), "684)&&*%/.,".getBytes(StandardCharsets.UTF_8)},
                {"HEADER".getBytes(StandardCharsets.UTF_8), null},
                {new byte[0], null}
        };
    }

    @Test(dataProvider = "data")
    public void wrap_thenUnwrap_inputEqualsOutput(byte[] header, byte[] body) {
        DataWrapper wrapper = new DataWrapper();
        Envelope envelope = new Envelope(header, body);

        byte[] wrapped = wrapper.wrap(envelope);
        Envelope unwrapped = wrapper.unwrap(wrapped);

        Assert.assertEquals(unwrapped.getHeader(), header);
        Assert.assertEquals(unwrapped.getBody(), body);
    }

    @Test
    public void unwrap_emptyArray_returnsEmptyEnvelope() {
        DataWrapper wrapper = new DataWrapper();
        Envelope envelope = wrapper.unwrap(new byte[0]);
        Assert.assertEquals(envelope.getHeader().length, 0);
        Assert.assertNull(envelope.getBody());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Data cannot be null")
    public void unwrap_inputIsNull_throwsException() {
        DataWrapper wrapper = new DataWrapper();
        wrapper.unwrap(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Not enough data in length descriptor. Expected 2 but found 1")
    public void unwrap_lengthDescriptorIsTooShort_throwsException() {
        DataWrapper wrapper = new DataWrapper();
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(2);
        res.write(10);
        byte[] data = res.toByteArray();

        wrapper.unwrap(data);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Not enough data in header. Expected 10 but found 1")
    public void unwrap_headerLengthIsTooShort_throwsException() {
        DataWrapper wrapper = new DataWrapper();
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(1);
        res.write(10);
        res.write(222);
        byte[] data = res.toByteArray();
        wrapper.unwrap(data);
    }

}