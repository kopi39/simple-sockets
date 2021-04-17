package org.kopi.web.tcp.async.logic.itf;

@FunctionalInterface
public interface Receiver {
    boolean process(byte[] data);
}
