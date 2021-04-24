package org.kopi.socket.ctype.tcp.async.itf;

@FunctionalInterface
public interface Sender {
    void send(byte[] data);
}
