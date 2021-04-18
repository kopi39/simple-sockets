package org.kopi.socket.itf;

public interface SocketServer extends AutoCloseable {
    void start(int port);

    void stopServer();

    void close();
}
