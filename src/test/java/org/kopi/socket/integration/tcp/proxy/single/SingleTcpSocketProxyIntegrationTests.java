package org.kopi.socket.integration.tcp.proxy.single;

import org.kopi.socket.ctype.tcp.async.itf.Producer;
import org.kopi.socket.ctype.tcp.async.itf.Receiver;
import org.kopi.socket.ctype.tcp.proxy.itf.Interceptor;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.integration.tcp.general.InterceptorMock;
import org.kopi.socket.integration.tcp.general.ProducerMock;
import org.kopi.socket.integration.tcp.general.ReceiverMock;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.util.async.Async;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.io.SafeClose;
import org.kopi.socket.util.security.NoEncryptionService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class SingleTcpSocketProxyIntegrationTests {

    private final List<String> serverMessages = Arrays.asList("server", "X", "messages");
    private ReceiverMock serverReceiver;
    private SocketServer server;
    private SocketServer proxy;
    private Thread proxyThread;
    private Thread serverThread;

    @BeforeMethod
    public void setUp() throws InterruptedException {
        ProducerMock serverProducer = createProducer(serverMessages);
        serverReceiver = createReceiver();
        server = createServer(serverProducer, serverReceiver);
        proxy = createProxy();
        serverThread = Async.start(() -> server.start(IntegrationTestsConfig.PORT));
        proxyThread = Async.start(() -> proxy.start(IntegrationTestsConfig.PROXY_PORT));
        Thread.sleep(100); // give servers some time to start
    }

    @AfterMethod
    public void cleanUp() throws InterruptedException {
        server.stopServer();
        proxy.stopServer();
        SafeClose.close(server, proxy);
        serverThread.join();
        proxyThread.join();
    }

    @Test
    public void testSingleConnection() {
        List<String> clientMessages = Arrays.asList("X", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createClient(clientProducer, clientReceiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_PORT);
        }

        List<String> messagesReceivedByClient = clientReceiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        List<String> expectedOnServer = Arrays.asList("-+from client+-", "-+TO+-", "-+server*/*-/$^%#$+-");
        List<String> expectedOnClient = Arrays.asList("+-server-+", "+-messages-+");

        Assert.assertEquals(messagesReceivedByClient, expectedOnClient, messagesReceivedByClient.toString());
        Assert.assertEquals(messagesReceivedByServer, expectedOnServer, messagesReceivedByServer.toString());
    }

    @Test
    public void testReconnection() {
        List<String> client1Messages = Arrays.asList("X", "from client", "TO", "server*/*-/$^%#$");
        List<String> client2Messages = Arrays.asList("CLIENT", "2", "X", "messages");
        ProducerMock client1Producer = createProducer(client1Messages);
        ProducerMock client2Producer = createProducer(client2Messages);
        ReceiverMock client1Receiver = createReceiver();
        ReceiverMock client2Receiver = createReceiver();

        try (SocketClient client1 = createClient(client1Producer, client1Receiver)) {
            client1.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_PORT);
        }

        try (SocketClient client2 = createClient(client2Producer, client2Receiver)) {
            client2.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_PORT);
        }

        List<String> messagesReceivedByClient1 = client1Receiver.getReceivedMessages();
        List<String> messagesReceivedByClient2 = client2Receiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        List<String> expectedOnServer = Arrays.asList("-+from client+-", "-+TO+-", "-+server*/*-/$^%#$+-", "-+CLIENT+-", "-+2+-", "-+messages+-");
        List<String> expectedOnClient = Arrays.asList("+-server-+", "+-messages-+");

        Assert.assertEquals(messagesReceivedByClient1, expectedOnClient, messagesReceivedByClient1.toString());
        Assert.assertEquals(messagesReceivedByClient2, expectedOnClient, messagesReceivedByClient2.toString());
        Assert.assertEquals(messagesReceivedByServer, expectedOnServer, messagesReceivedByServer.toString());
    }

    private SocketServer createProxy() {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        Interceptor interceptor1 = new InterceptorMock(0, 1, "+", encodingService);
        Interceptor interceptor2 = new InterceptorMock(1, 0, "-", encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory();
        return socketFactory.createProxy(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT, interceptor1, interceptor2);
    }

    private ProducerMock createProducer(List<String> messagesToSend) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        return new ProducerMock(messagesToSend, encodingService);
    }

    private ReceiverMock createReceiver() {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        return new ReceiverMock(encodingService);
    }

    private SocketServer createServer(Producer producer, Receiver receiver) {
        NoEncryptionService encryptionService = new NoEncryptionService();
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createAsyncServer(() -> producer, () -> receiver);
    }

    private SocketClient createClient(Producer producer, Receiver receiver) {
        NoEncryptionService encryptionService = new NoEncryptionService();
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createAsyncClient(producer, receiver, false);
    }

}
