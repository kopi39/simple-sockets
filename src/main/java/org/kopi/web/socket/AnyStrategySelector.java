package org.kopi.web.socket;

import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.socket.itf.StrategySelector;

import java.net.Socket;
import java.util.List;

public class AnyStrategySelector implements StrategySelector {

    @Override
    public SocketStrategy select(Socket socket, List<SocketStrategy> strategies) {
        return strategies.get(0);
    }
}
