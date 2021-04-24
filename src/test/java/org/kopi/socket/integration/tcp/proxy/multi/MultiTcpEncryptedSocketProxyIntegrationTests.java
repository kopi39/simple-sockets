package org.kopi.socket.integration.tcp.proxy.multi;

import org.kopi.socket.ctype.tcp.async.itf.Producer;
import org.kopi.socket.ctype.tcp.async.itf.Receiver;
import org.kopi.socket.general.TcpSocketFactory;
import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.integration.tcp.general.ProducerMock;
import org.kopi.socket.integration.tcp.general.ReceiverMock;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.util.async.Async;
import org.kopi.socket.util.encoding.Utf8EncodingService;
import org.kopi.socket.util.io.SafeClose;
import org.kopi.socket.util.security.AesEncryptionService;
import org.kopi.socket.util.security.NoEncryptionService;
import org.kopi.socket.util.security.itf.EncryptionService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class MultiTcpEncryptedSocketProxyIntegrationTests {

    private final List<String> serverMessages = Arrays.asList("server", "X", "messages");
    private ReceiverMock serverReceiver;
    private SocketServer server;
    private SocketServer proxy1;
    private SocketServer proxy2;
    private Thread proxy1Thread;
    private Thread proxy2Thread;
    private Thread serverThread;

    @BeforeMethod
    public void setUp() throws InterruptedException {
        ProducerMock serverProducer = createProducer(serverMessages);
        serverReceiver = createReceiver();
        server = createServer(serverProducer, serverReceiver);
        proxy1 = createServerProxy();
        proxy2 = createClientProxy();
        serverThread = Async.start(() -> server.start(IntegrationTestsConfig.PORT));
        proxy1Thread = Async.start(() -> proxy1.start(IntegrationTestsConfig.PROXY_PORT));
        proxy2Thread = Async.start(() -> proxy2.start(IntegrationTestsConfig.PROXY_2_PORT));
        Thread.sleep(100); // give servers some time to start
    }

    @AfterMethod
    public void cleanUp() throws InterruptedException {
        server.stopServer();
        proxy1.stopServer();
        proxy2.stopServer();
        SafeClose.close(server, proxy1, proxy2);
        serverThread.join();
        proxy1Thread.join();
        proxy2Thread.join();
    }

    @Test
    public void testSingleConnection() {
        List<String> clientMessages = Arrays.asList("X", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createClient(clientProducer, clientReceiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_2_PORT);
        }

        List<String> messagesReceivedByClient = clientReceiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        Assert.assertEquals(messagesReceivedByClient, serverMessages, messagesReceivedByClient.toString());
        Assert.assertEquals(messagesReceivedByServer, clientMessages, messagesReceivedByServer.toString());
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
            client1.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_2_PORT);
        }

        try (SocketClient client2 = createClient(client2Producer, client2Receiver)) {
            client2.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_2_PORT);
        }

        List<String> messagesReceivedByClient1 = client1Receiver.getReceivedMessages();
        List<String> messagesReceivedByClient2 = client2Receiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        List<String> expectedOnServer = Arrays.asList("X", "from client", "TO", "server*/*-/$^%#$", "CLIENT", "2", "X", "messages");

        Assert.assertEquals(messagesReceivedByClient1, serverMessages, messagesReceivedByClient1.toString());
        Assert.assertEquals(messagesReceivedByClient2, serverMessages, messagesReceivedByClient2.toString());
        Assert.assertEquals(messagesReceivedByServer, expectedOnServer, messagesReceivedByServer.toString());
    }

    private SocketServer createServerProxy() {
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createServerProxy(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
    }

    private SocketServer createClientProxy() {
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createClientProxy(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PROXY_PORT, false);
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
