package org.kopi.socket.itf;

public interface SyncSocketClient extends SocketClient {
    byte[] send(byte[] data);
}
