package org.kopi.socket.data.wrap;

import org.kopi.util.io.BytesUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DataWrapper {

    // headerLengthLength + headerLength + header + body -> byte[]
    public byte[] wrap(Envelope envelope) {
        int headerLength = envelope.getHeader().length;
        byte[] headerLengthArray = BytesUtil.intToByteArray(headerLength);
        int lenLen = headerLengthArray.length;
        try (ByteArrayOutputStream res = new ByteArrayOutputStream()) {
            res.write(lenLen);
            res.write(headerLengthArray);
            res.write(envelope.getHeader());
            byte[] body = envelope.getBody();
            if (body != null) {
                res.write(body);
            }
            return res.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Envelope unwrap(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }

        if (data.length == 0) {
            return new Envelope(data, null);
        }

        int off = 1;
        int lenLen = Byte.toUnsignedInt(data[0]);
        if (data.length <= lenLen) {
            throw new IllegalArgumentException("Not enough data in length descriptor. Expected " + lenLen + " but found " + (data.length - off));
        }

        byte[] lenBytes = copy(data, off, lenLen);
        int headerLength = BytesUtil.byteArrayToInt(lenBytes);
        off += lenLen;

        if (data.length - off < headerLength) {
            throw new IllegalArgumentException("Not enough data in header. Expected " + headerLength + " but found " + (data.length - off));
        }

        byte[] header = copy(data, off, headerLength);
        off += headerLength;
        byte[] body = data.length > off ? Arrays.copyOfRange(data, off, data.length) : null;
        return new Envelope(header, body);
    }

    private byte[] copy(byte[] array, int offset, int length) {
        return Arrays.copyOfRange(array, offset, length + offset);
    }

}
