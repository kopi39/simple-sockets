package org.kopi.web.socket;

import org.kopi.util.io.SafeClose;
import org.kopi.web.socket.itf.SocketServer;
import org.kopi.web.socket.itf.SocketStrategy;
import org.kopi.web.socket.itf.StrategySelector;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class OneToOneSocketServer implements SocketServer {

    private static final int SERVER_BACKLOG = 1;

    private final AtomicBoolean stopServer = new AtomicBoolean(false);
    private final List<SocketStrategy> strategies = new ArrayList<>();
    private final StrategySelector strategySelector;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public OneToOneSocketServer(StrategySelector strategySelector, SocketStrategy strategy, SocketStrategy... otherStrategies) {
        this.strategies.add(strategy);
        this.strategies.addAll(Arrays.asList(otherStrategies));
        this.strategySelector = strategySelector;
    }

    @Override
    public void start(int port) {
        initServer(port);
        while (!stopServer.get()) {
            listenForClient();
        }
    }

    @Override
    public void stopServer() {
        this.stopServer.set(true);
        close();
    }

    @Override
    public void close() {
        closeClient();
        SafeClose.close(serverSocket);
    }

    private void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port, SERVER_BACKLOG);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void listenForClient() {
        try {
            clientSocket = serverSocket.accept();
            SocketStrategy strategy = this.strategySelector.select(clientSocket, this.strategies);
            SocketStrategy.Result result = strategy.apply(clientSocket);
            if (result.isStopServer()) {
                this.stopServer();
                return;
            }
            closeClient();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void closeClient() {
        this.strategies.forEach(SocketStrategy::close);
        SafeClose.close(clientSocket);
    }
}
