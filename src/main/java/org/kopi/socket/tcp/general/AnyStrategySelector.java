package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.StrategySelector;

import java.net.Socket;
import java.util.List;

public class AnyStrategySelector implements StrategySelector {

    @Override
    public StrategyWrapper select(Socket socket, List<StrategyWrapper> strategies) {
        return strategies.get(0);
    }
}
