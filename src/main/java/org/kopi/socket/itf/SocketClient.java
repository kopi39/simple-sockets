package org.kopi.socket.itf;

import java.io.Closeable;

public interface SocketClient extends AutoCloseable, Closeable {
    void connect(String host, int port);

    void close();
}
