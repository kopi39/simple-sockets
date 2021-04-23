package org.kopi.socket.data.wrap;

import org.testng.annotations.Test;

public class EnvelopeTest {

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Header cannot be null")
    public void createEnvelope_withNullHeader_throwException() {
        new Envelope(null, new byte[0]);
    }

}