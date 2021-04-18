package org.kopi.socket.itf;

import java.io.Closeable;

public interface SocketServer extends AutoCloseable, Closeable {
    void start(int port);

    void stopServer();

    void close();
}
