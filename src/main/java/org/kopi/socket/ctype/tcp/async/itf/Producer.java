package org.kopi.socket.ctype.tcp.async.itf;

@FunctionalInterface
public interface Producer {
    void process(Sender sender);
}
