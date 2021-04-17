package org.kopi.web.tcp.async.logic.itf;

@FunctionalInterface
public interface Producer {
    void process(Sender sender);
}
