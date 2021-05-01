package org.kopi.socket.general;

import org.kopi.socket.general.ex.SimpleSocketException;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.itf.StrategySelector;
import org.kopi.socket.itf.StrategySupplier;
import org.kopi.socket.util.async.Async;
import org.kopi.socket.util.io.SafeClose;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServerImpl implements SocketServer {

    private static final int TIMEOUT = 100;
    private static final int SERVER_BACKLOG = 50;

    private final AtomicBoolean stopServer = new AtomicBoolean(false);
    private final List<StrategyWrapper> strategies = new ArrayList<>();
    private final StrategySelector strategySelector;

    private final List<Client> clients = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> clientThreads = Collections.synchronizedList(new ArrayList<>());

    private ServerSocket serverSocket;
    private int maxConnections;

    public SocketServerImpl(StrategySelector selector, StrategySupplier strategy, StrategySupplier... other) {
        this.strategySelector = selector;
        this.strategies.add(StrategyWrapper.wrap(strategy));
        Arrays.stream(other).map(StrategyWrapper::wrap).forEach(this.strategies::add);
    }

    @Override
    public void start(int port) {
        this.start(port, 1);
    }

    @Override
    public void start(int port, int maxConnections) {
        this.maxConnections = maxConnections;
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
        clients.forEach(this::closeClient);
        Async.interruptAll(clientThreads);
        Async.joinAll(clientThreads);
        clients.clear();
        SafeClose.close(serverSocket);
    }

    private void initServer(int port) {
        try {
            this.serverSocket = new ServerSocket(port, SERVER_BACKLOG);
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }

    private void listenForClient() {
        try {
            clientThreads.removeIf(x -> !x.isAlive());
            Client client = new Client();
            client.socket = serverSocket.accept();
            if (!allowConnection()) {
                client.socket.close();
                return;
            }
            this.clients.add(client);
            Thread clientThread = Async.start(() -> this.handleClient(client));
            clientThreads.add(clientThread);
        } catch (SocketException ex) {
            String exMsg = ex.getMessage();
            if (!"Socket closed".equalsIgnoreCase(exMsg) && !exMsg.startsWith("Interrupted function call")) {
                throw new SimpleSocketException(ex);
            }
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }

    private boolean allowConnection() {
        for (int i = 0; i < TIMEOUT; ++i) {
            if (clients.size() < maxConnections) {
                return true;
            }
            Async.sleep(1);
        }
        return false;
    }

    private void handleClient(Client client) {
        try {
            StrategyWrapper supplier = strategySelector.select(client.socket, this.strategies);
            client.strategy = supplier.createStrategy();
            SocketStrategy.Result result = client.strategy.apply(client.socket);
            if (result.isStopServer()) {
                this.stopServer();
            }
        } finally {
            closeClient(client);
            this.clients.remove(client);
        }
    }

    private void closeClient(Client client) {
        SafeClose.close(client.strategy, client.socket);
    }

    private static class Client {
        private Socket socket;
        private SocketStrategy strategy;
    }

}
