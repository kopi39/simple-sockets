package org.kopi.socket.tcp.strategies.async.itf;

@FunctionalInterface
public interface Producer {
    void process(Sender sender);
}
