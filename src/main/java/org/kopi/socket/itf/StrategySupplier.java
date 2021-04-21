package org.kopi.socket.itf;

@FunctionalInterface
public interface StrategySupplier {
    SocketStrategy create();
}
