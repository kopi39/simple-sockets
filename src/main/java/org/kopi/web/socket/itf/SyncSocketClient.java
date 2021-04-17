package org.kopi.web.socket.itf;

public interface SyncSocketClient extends SocketClient {
    byte[] send(byte[] data);
}
