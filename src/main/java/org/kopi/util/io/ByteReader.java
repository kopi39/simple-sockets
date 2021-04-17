package org.kopi.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ByteReader implements AutoCloseable {

    private final InputStream is;
    private final byte[] buff;

    public ByteReader(InputStream is) {
        this.is = is;
        this.buff = new byte[1024];
    }

    public Optional<byte[]> read() throws IOException {
        int len = is.read(buff);
        if (len < 0) {
            return Optional.empty();
        }

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        res.write(buff, 0, len);
        int available = is.available();
        while (available > 0) {
            res.write(is.readNBytes(available));
            available = is.available();
        }

        return Optional.of(res.toByteArray());
    }

    @Override
    public void close() throws IOException {
        if (is != null) {
            is.close();
        }
    }
}
