package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.itf.SocketStrategy;
import org.kopi.socket.itf.StrategySelector;
import org.kopi.socket.tcp.strategies.async.AsyncStrategy;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Receiver;
import org.kopi.socket.tcp.strategies.sync.ProducerSyncStrategy;
import org.kopi.socket.tcp.strategies.sync.ReceiverSyncStrategy;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.socket.tcp.strategies.sync.itf.SyncReceiver;
import org.kopi.util.security.NoEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

import java.io.OutputStream;
import java.net.Socket;

public class TcpSocketFactory {

    private EncryptionService encryptionService;

    public TcpSocketFactory() {
        this.encryptionService = new NoEncryptionService();
    }

    public TcpSocketFactory(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public TcpSocketFactory setEncryptionService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
        return this;
    }

    public SocketServer createSyncServer(SyncReceiver syncReceiver) {
        StrategySelector strategySelector = new AnyStrategySelector();
        SocketStrategy syncStrategy = new ReceiverSyncStrategy(syncReceiver, encryptionService);
        return new OneToOneSocketServer(strategySelector, syncStrategy);
    }

    public SocketServer createAsyncServer(Producer producer, Receiver receiver) {
        StrategySelector strategySelector = new AnyStrategySelector();
        SocketStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);
        return new OneToOneSocketServer(strategySelector, asyncStrategy);
    }

    public SocketServer createMixServer(Producer producer, Receiver receiver, SyncReceiver syncReceiver) {
        StrategySelector strategySelector = new FirstByteStrategySelector();
        SocketStrategy syncStrategy = new ReceiverSyncStrategy(syncReceiver, encryptionService);
        SocketStrategy asyncStrategy = new AsyncStrategy(producer, receiver, encryptionService);
        return new OneToOneSocketServer(strategySelector, syncStrategy, asyncStrategy);
    }

    public SocketClient createSyncClient(SyncProducer syncProducer, boolean serverIsMixed) {
        SocketStrategy strategy = new ProducerSyncStrategy(syncProducer, encryptionService);
        TcpSocketClient.OnConnect onConnect = getOnConnect(ReceiverSyncStrategy.CODE, serverIsMixed);
        return new TcpSocketClient(strategy, onConnect);
    }

    public SocketClient createAsyncClient(Producer producer, Receiver receiver, boolean serverIsMixed) {
        SocketStrategy strategy = new AsyncStrategy(producer, receiver, encryptionService);
        TcpSocketClient.OnConnect onConnect = getOnConnect(AsyncStrategy.CODE, serverIsMixed);
        return new TcpSocketClient(strategy, onConnect);
    }

    private TcpSocketClient.OnConnect getOnConnect(int serverStrategyCode, boolean serverIsMixed) {
        if (!serverIsMixed) {
            return x -> {
            };
        }
        return x -> onConnect(x, serverStrategyCode);
    }

    private void onConnect(Socket clientSocket, int code) throws Exception {
        OutputStream out = clientSocket.getOutputStream();
        out.write(code);
        out.flush();
    }
}
