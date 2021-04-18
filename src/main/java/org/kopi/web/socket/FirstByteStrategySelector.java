package org.kopi.web.socket;

import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.socket.itf.StrategySelector;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class FirstByteStrategySelector implements StrategySelector {

    @Override
    public SocketStrategy select(Socket socket, List<SocketStrategy> strategies) {
        try {
            InputStream in = socket.getInputStream();
            int code = in.read();
            for (SocketStrategy strategy : strategies) {
                if (strategy.getStrategyCode() == code) {
                    return strategy;
                }
            }
            throw new RuntimeException("Strategy not found: " + code);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
