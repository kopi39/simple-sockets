package org.kopi.web.socket.itf;

public interface SocketServer extends AutoCloseable {
    void start(int port);

    void stopServer();

    void close();
}
