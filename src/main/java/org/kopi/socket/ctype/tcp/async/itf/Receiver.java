package org.kopi.socket.ctype.tcp.async.itf;

@FunctionalInterface
public interface Receiver {
    boolean process(byte[] data);
}
