package org.kopi.web.tcp.async.logic.itf;

@FunctionalInterface
public interface Sender {
    void send(byte[] data);
}
