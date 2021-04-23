package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.itf.StrategySupplier;

public class StrategyWrapper {

    private final int strategyCode;
    private final StrategySupplier strategySupplier;

    public StrategyWrapper(int strategyCode, StrategySupplier strategySupplier) {
        this.strategyCode = strategyCode;
        this.strategySupplier = strategySupplier;
    }

    public static StrategyWrapper wrap(StrategySupplier strategySupplier) {
        int code;
        try (SocketStrategy strategy = strategySupplier.create()) {
            code = strategy.getStrategyCode();
        }
        return new StrategyWrapper(code, strategySupplier);
    }

    public int getStrategyCode() {
        return strategyCode;
    }

    public SocketStrategy createStrategy() {
        return strategySupplier.create();
    }

}
