package org.kopi.socket.itf;

import java.io.Closeable;

public interface SocketServer extends AutoCloseable, Closeable {
    void start(int port);

    void start(int port, int maxConnections);

    void stopServer();

    void close();
}
