package org.kopi.socket.tcp.strategies.async.itf;

@FunctionalInterface
public interface Receiver {
    boolean process(byte[] data);
}
