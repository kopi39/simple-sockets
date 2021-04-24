package org.kopi.socket.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Optional;

public class BytesUtil {

    public static Optional<byte[]> readDynamic(InputStream is) throws IOException {
        try {
            int len = is.read();
            if (len < 0) {
                return Optional.empty();
            }
            byte[] msgSizeBytes = new byte[len];
            int size = is.read(msgSizeBytes);
            if (len != size) {
                throw new RuntimeException("Incorrect size. Expected " + len + " but got " + size);
            }
            int bodySize = byteArrayToInt(msgSizeBytes);
            byte[] body = new byte[bodySize];
            int off = is.read(body, 0, bodySize);
            while (off < bodySize) {
                off += is.read(body, off, bodySize - off);
            }
            return Optional.of(body);
        } catch (SocketException ex) {
            if ("Socket closed".equalsIgnoreCase(ex.getMessage())) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public static Optional<byte[]> read(InputStream is, byte[] buff) throws IOException {
        try {
            int len = is.read(buff);
            if (len < 0) {
                return Optional.empty();
            }
            return Optional.of(Arrays.copyOf(buff, len));
        } catch (SocketException ex) {
            if ("Socket closed".equalsIgnoreCase(ex.getMessage())) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public static void writeDynamic(OutputStream out, byte[] body) throws IOException {
        if (body == null) {
            return;
        }
        ByteArrayOutputStream res = new ByteArrayOutputStream();
        int bodyLen = body.length;
        byte[] bodyLenArray = BytesUtil.intToByteArray(bodyLen);
        int lenLen = bodyLenArray.length;
        res.write(lenLen);
        res.write(bodyLenArray);
        res.write(body);
        out.write(res.toByteArray());
        out.flush();
    }

    public static void write(OutputStream out, byte[] body) throws IOException {
        if (body != null) {
            out.write(body);
            out.flush();
        }
    }

    public static int byteArrayToInt(byte[] bytes) {
        return new BigInteger(bytes).intValue();
    }

    public static byte[] intToByteArray(int value) {
        return BigInteger.valueOf(value).toByteArray();
    }
}
