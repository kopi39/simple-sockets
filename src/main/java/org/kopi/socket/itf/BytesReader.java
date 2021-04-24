package org.kopi.socket.itf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

@FunctionalInterface
public interface BytesReader {
    Optional<byte[]> read(InputStream in) throws IOException;
}
