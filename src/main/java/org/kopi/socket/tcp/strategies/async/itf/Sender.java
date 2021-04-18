package org.kopi.socket.tcp.strategies.async.itf;

@FunctionalInterface
public interface Sender {
    void send(byte[] data);
}
