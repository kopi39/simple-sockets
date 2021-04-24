package org.kopi.socket.tcp.general;

import org.kopi.socket.itf.*;
import org.kopi.socket.tcp.proxy.ProxyStrategy;
import org.kopi.socket.tcp.proxy.ProxyStrategy.ProxyType;
import org.kopi.socket.tcp.proxy.interceptors.EncryptIncomingDecryptOutgoingInterceptor;
import org.kopi.socket.tcp.proxy.interceptors.EncryptOutgoingDecryptIncomingInterceptor;
import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.socket.tcp.strategies.async.AsyncStrategy;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Receiver;
import org.kopi.socket.tcp.strategies.sync.ProducerSyncStrategy;
import org.kopi.socket.tcp.strategies.sync.ReceiverSyncStrategy;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.socket.tcp.strategies.sync.itf.SyncReceiver;
import org.kopi.util.io.BytesUtil;
import org.kopi.util.security.NoEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

import java.io.OutputStream;
import java.net.Socket;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TcpSocketFactory {

    private static final int BUFF_SIZE = 1024;

    private Supplier<BytesReader> reader = () -> BytesUtil::readDynamic;
    private Supplier<BytesWriter> writer = () -> BytesUtil::writeDynamic;

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

    public TcpSocketFactory useSimpleReaderAndWriter() {
        this.reader = this::createSimpleReader;
        this.writer = this::createSimpleWriter;
        return this;
    }

    public TcpSocketFactory setReader(Supplier<BytesReader> reader) {
        this.reader = reader;
        return this;
    }

    public TcpSocketFactory setWriter(Supplier<BytesWriter> writer) {
        this.writer = writer;
        return this;
    }

    public SocketServer createSyncServer(Supplier<SyncReceiver> syncReceiver) {
        StrategySelector strategySelector = new AnyStrategySelector();
        StrategySupplier supplier = () -> new ReceiverSyncStrategy(syncReceiver.get(), reader.get(), writer.get(), encryptionService);
        return new SocketServerImpl(strategySelector, this::doNothing, supplier);
    }

    public SocketServer createAsyncServer(Supplier<Producer> producer, Supplier<Receiver> receiver) {
        StrategySelector strategySelector = new AnyStrategySelector();
        StrategySupplier supplier = () -> new AsyncStrategy(producer.get(), receiver.get(), reader.get(), writer.get(), encryptionService);
        return new SocketServerImpl(strategySelector, this::doNothing, supplier);
    }

    public SocketServer createMixServer(Supplier<Producer> producer, Supplier<Receiver> receiver, Supplier<SyncReceiver> syncReceiver) {
        StrategySelector strategySelector = new FirstByteStrategySelector();
        StrategySupplier syncSupplier = () -> new ReceiverSyncStrategy(syncReceiver.get(), reader.get(), writer.get(), encryptionService);
        StrategySupplier asyncSupplier = () -> new AsyncStrategy(producer.get(), receiver.get(), BytesUtil::readDynamic, BytesUtil::writeDynamic, encryptionService);
        return new SocketServerImpl(strategySelector, this::doNothing, syncSupplier, asyncSupplier);
    }

    public SocketServer createProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        StrategySupplier supplier = () -> new ProxyStrategy(host, port, reader.get(), writer.get(), interceptors);
        return new SocketServerImpl(selector, this::doNothing, supplier);
    }

    public SocketServer createServerProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        Interceptor encryption = new EncryptIncomingDecryptOutgoingInterceptor(encryptionService);
        Interceptor[] interceptorsArray = concatInterceptors(interceptors, encryption);
        StrategySupplier supplier = () -> new ProxyStrategy(
                host,
                port,
                ProxyType.SERVER,
                reader.get(),
                writer.get(),
                createSimpleReader(),
                createSimpleWriter(),
                interceptorsArray);
        return new SocketServerImpl(selector, this::doNothing, supplier);
    }

    public SocketServer createClientProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        Interceptor encryption = new EncryptOutgoingDecryptIncomingInterceptor(encryptionService);
        Interceptor[] interceptorsArray = concatInterceptors(interceptors, encryption);
        StrategySupplier supplier = () -> new ProxyStrategy(
                host,
                port,
                ProxyType.CLIENT,
                reader.get(),
                writer.get(),
                createSimpleReader(),
                createSimpleWriter(),
                interceptorsArray);
        return new SocketServerImpl(selector, this::doNothing, supplier);
    }

    public SocketClient createSyncClient(SyncProducer syncProducer, boolean serverIsMixed) {
        SocketStrategy strategy = new ProducerSyncStrategy(syncProducer, reader.get(), writer.get(), encryptionService);
        OnConnect onConnect = getOnConnect(ReceiverSyncStrategy.CODE, serverIsMixed);
        return new TcpSocketClient(strategy, onConnect);
    }

    public SocketClient createAsyncClient(Producer producer, Receiver receiver, boolean serverIsMixed) {
        SocketStrategy strategy = new AsyncStrategy(producer, receiver, reader.get(), writer.get(), encryptionService);
        OnConnect onConnect = getOnConnect(AsyncStrategy.CODE, serverIsMixed);
        return new TcpSocketClient(strategy, onConnect);
    }

    private Interceptor[] concatInterceptors(Interceptor[] interceptors, Interceptor... other) {
        return Stream.of(interceptors, other).flatMap(Stream::of).toArray(Interceptor[]::new);
    }

    private OnConnect getOnConnect(int serverStrategyCode, boolean serverIsMixed) {
        if (!serverIsMixed) {
            return this::doNothing;
        }
        return x -> onConnect(x, serverStrategyCode);
    }

    private void doNothing(Socket socket) {

    }

    private void onConnect(Socket clientSocket, int code) throws Exception {
        OutputStream out = clientSocket.getOutputStream();
        out.write(code);
        out.flush();
    }

    private BytesReader createSimpleReader() {
        return x -> BytesUtil.read(x, new byte[BUFF_SIZE]);
    }

    private BytesWriter createSimpleWriter() {
        return BytesUtil::write;
    }
}
