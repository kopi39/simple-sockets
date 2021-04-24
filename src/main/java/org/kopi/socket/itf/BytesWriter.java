package org.kopi.socket.itf;

import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface BytesWriter {
    void write(OutputStream out, byte[] data) throws IOException;
}
