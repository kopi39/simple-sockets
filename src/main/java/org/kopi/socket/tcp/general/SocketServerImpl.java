package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.*;
import org.kopi.util.async.Async;
import org.kopi.util.io.SafeClose;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketServerImpl implements SocketServer {

    private static final int SERVER_BACKLOG = 50;

    private final AtomicBoolean stopServer = new AtomicBoolean(false);
    private final List<StrategyWrapper> strategies = new ArrayList<>();
    private final StrategySelector strategySelector;
    private final OnConnect onConnect;

    private final List<Client> clients = Collections.synchronizedList(new ArrayList<>());
    private final List<Thread> clientThreads = Collections.synchronizedList(new ArrayList<>());

    private ServerSocket serverSocket;
    private int maxConnections;


    public SocketServerImpl(StrategySelector selector, OnConnect onConnect, StrategySupplier strategy, StrategySupplier... other) {
        this.strategySelector = selector;
        this.onConnect = onConnect;
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
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void listenForClient() {
        try {
            clientThreads.removeIf(x -> !x.isAlive());
            Client client = new Client();
            client.socket = serverSocket.accept();
            if (clients.size() >= maxConnections) {
                client.socket.close();
                return;
            }
            this.clients.add(client);
            this.onConnect.invoke(client.socket);
            Thread clientThread = Async.start(() -> this.handleClient(client));
            clientThreads.add(clientThread);
        } catch (SocketException ex) {
            if (!"Socket closed".equalsIgnoreCase(ex.getMessage())) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleClient(Client client) {
        try {
            StrategyWrapper supplier = strategySelector.select(client.socket, this.strategies);
            client.strategy = supplier.createStrategy();
            SocketStrategy.Result result = client.strategy.apply(client.socket);
            if (result.isStopServer()) {
                this.stopServer();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
