package org.kopi.socket.general;

import org.kopi.socket.general.ex.SimpleSocketException;
import org.kopi.socket.itf.StrategySelector;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class FirstByteStrategySelector implements StrategySelector {

    @Override
    public StrategyWrapper select(Socket socket, List<StrategyWrapper> strategies) {
        try {
            InputStream in = socket.getInputStream();
            int code = in.read();
            for (StrategyWrapper strategy : strategies) {
                if (strategy.getStrategyCode() == code) {
                    return strategy;
                }
            }
            throw new SimpleSocketException("Strategy not found: " + code);
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }
}
