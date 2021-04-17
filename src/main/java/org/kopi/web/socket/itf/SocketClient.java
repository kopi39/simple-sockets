package org.kopi.web.socket.itf;

public interface SocketClient extends AutoCloseable {
    void connect(String host, int port);

    void close();
}
