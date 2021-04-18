package org.kopi.socket.itf;

import java.net.Socket;
import java.util.List;

public interface StrategySelector {

    SocketStrategy select(Socket socket, List<SocketStrategy> strategies);

}
