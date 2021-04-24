package org.kopi.socket.itf;

import org.kopi.socket.general.StrategyWrapper;

import java.net.Socket;
import java.util.List;

public interface StrategySelector {

    StrategyWrapper select(Socket socket, List<StrategyWrapper> strategies);

}
