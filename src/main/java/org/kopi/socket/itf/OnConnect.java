package org.kopi.socket.itf;

import java.io.IOException;
import java.net.Socket;

@FunctionalInterface
public interface OnConnect {
    void invoke(Socket socket) throws IOException;
}
