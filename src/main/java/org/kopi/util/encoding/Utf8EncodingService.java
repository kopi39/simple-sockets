package org.kopi.util.encoding;

import org.kopi.util.encoding.itf.EncodingService;

import java.nio.charset.StandardCharsets;

public class Utf8EncodingService implements EncodingService<String, byte[]> {

    @Override
    public byte[] encode(String input) {
        if (input == null) {
            return null;
        }
        return input.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decode(byte[] output) {
        if (output == null) {
            return null;
        }
        return new String(output, StandardCharsets.UTF_8);
    }
}
