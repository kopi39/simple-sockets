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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TcpSocketFactory {

    private static final int BUFF_SIZE = 1024;

    private final StrategyFactory strategyFactory = new StrategyFactory();

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
        StrategySupplier supplier = strategyFactory.createSyncStrategy(syncReceiver);
        return new SocketServerImpl(strategySelector, this::doNothing, supplier);
    }

    public SocketServer createAsyncServer(Supplier<Producer> producer, Supplier<Receiver> receiver) {
        StrategySelector strategySelector = new AnyStrategySelector();
        StrategySupplier supplier = strategyFactory.createAsyncStrategy(producer, receiver);
        return new SocketServerImpl(strategySelector, this::doNothing, supplier);
    }

    public Builder createMixServer() {
        return new Builder();
    }

    public SocketServer createProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        StrategySupplier supplier = strategyFactory.createPassingProxyStrategy(host, port, interceptors);
        return new SocketServerImpl(selector, this::doNothing, supplier);
    }

    public SocketServer createServerProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        StrategySupplier supplier = strategyFactory.createProxyServerStrategy(host, port, interceptors);
        return new SocketServerImpl(selector, this::doNothing, supplier);
    }

    public SocketServer createClientProxy(String host, int port, Interceptor... interceptors) {
        StrategySelector selector = new AnyStrategySelector();
        StrategySupplier supplier = strategyFactory.createProxyClientStrategy(host, port, interceptors);
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
        final byte[] buff = new byte[BUFF_SIZE];
        return x -> BytesUtil.read(x, buff);
    }

    private BytesWriter createSimpleWriter() {
        return BytesUtil::write;
    }

    public class StrategyFactory {

        public StrategySupplier createSyncStrategy(Supplier<SyncReceiver> syncReceiver) {
            return () -> new ReceiverSyncStrategy(
                    syncReceiver.get(),
                    reader.get(),
                    writer.get(),
                    encryptionService);
        }

        public StrategySupplier createAsyncStrategy(Supplier<Producer> producer, Supplier<Receiver> receiver) {
            return () -> new AsyncStrategy(
                    producer.get(),
                    receiver.get(),
                    reader.get(),
                    writer.get(),
                    encryptionService);
        }

        public StrategySupplier createProxyServerStrategy(String host, int port, Interceptor... interceptors) {
            Interceptor encryption = new EncryptIncomingDecryptOutgoingInterceptor(encryptionService);
            Interceptor[] interceptorsArray = concatInterceptors(interceptors, encryption);
            return () -> new ProxyStrategy(
                    host,
                    port,
                    ProxyType.SERVER,
                    reader.get(),
                    writer.get(),
                    createSimpleReader(),
                    createSimpleWriter(),
                    interceptorsArray);
        }

        public StrategySupplier createProxyClientStrategy(String host, int port, Interceptor... interceptors) {
            Interceptor encryption = new EncryptOutgoingDecryptIncomingInterceptor(encryptionService);
            Interceptor[] interceptorsArray = concatInterceptors(interceptors, encryption);
            return () -> new ProxyStrategy(
                    host,
                    port,
                    ProxyType.CLIENT,
                    reader.get(),
                    writer.get(),
                    createSimpleReader(),
                    createSimpleWriter(),
                    interceptorsArray);
        }

        public StrategySupplier createPassingProxyStrategy(String host, int port, Interceptor... interceptors) {
            return () -> new ProxyStrategy(host, port, reader.get(), writer.get(), interceptors);
        }
    }

    public class Builder {

        private final List<StrategySupplier> strategyList = new ArrayList<>();

        private Builder() {
            super();
        }

        public Builder addSyncStrategy(Supplier<SyncReceiver> syncReceiver) {
            return this.addStrategy(strategyFactory.createSyncStrategy(syncReceiver));
        }

        public Builder addAsyncStrategy(Supplier<Producer> producer, Supplier<Receiver> receiver) {
            return this.addStrategy(strategyFactory.createAsyncStrategy(producer, receiver));
        }

        public Builder addServerProxy(String host, int port, Interceptor... interceptors) {
            return this.addStrategy(strategyFactory.createProxyServerStrategy(host, port, interceptors));
        }

        public Builder addClientProxy(String host, int port, Interceptor... interceptors) {
            return this.addStrategy(strategyFactory.createProxyClientStrategy(host, port, interceptors));
        }

        public Builder addPassingProxy(String host, int port, Interceptor... interceptors) {
            return this.addStrategy(strategyFactory.createPassingProxyStrategy(host, port, interceptors));
        }

        public Builder addStrategy(StrategySupplier supplier) {
            this.strategyList.add(supplier);
            return this;
        }

        public SocketServer build() {
            if (strategyList.isEmpty()) {
                throw new IllegalArgumentException("At least one strategy must be defined");
            }
            StrategySupplier first = strategyList.remove(0);
            StrategySupplier[] others = strategyList.toArray(StrategySupplier[]::new);
            StrategySelector strategySelector = new FirstByteStrategySelector();
            return new SocketServerImpl(strategySelector, TcpSocketFactory.this::doNothing, first, others);
        }

    }
}
