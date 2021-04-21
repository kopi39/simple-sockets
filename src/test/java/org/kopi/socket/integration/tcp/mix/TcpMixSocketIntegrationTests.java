package org.kopi.socket.integration.tcp.mix;

import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.integration.tcp.general.ProducerMock;
import org.kopi.socket.integration.tcp.general.ReceiverMock;
import org.kopi.socket.integration.tcp.general.SyncProducerMock;
import org.kopi.socket.integration.tcp.general.SyncReceiverMock;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Receiver;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.util.async.Async;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TcpMixSocketIntegrationTests {

    private final List<String> serverMessages = Arrays.asList("Some", "SIMPLE", "mess555");
    private ReceiverMock serverReceiver;
    private SocketServer server;
    private Thread serverThread;

    @BeforeMethod
    public void setUp() throws InterruptedException {
        ProducerMock serverProducer = createProducer(serverMessages);
        serverReceiver = createReceiver();
        server = createServer(serverProducer, serverReceiver);
        serverThread = Async.start(() -> server.start(IntegrationTestsConfig.PORT));
        Thread.sleep(100); // give server some time to start
    }

    @AfterMethod
    public void cleanUp() throws InterruptedException {
        server.stopServer();
        SafeClose.close(server);
        serverThread.join();
    }

    @Test
    public void testSingleAsyncConnection() {
        List<String> clientMessages = Arrays.asList("Data", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createAsyncClient(clientProducer, clientReceiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> messagesReceivedByClient = clientReceiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        Assert.assertEquals(messagesReceivedByClient, serverMessages, messagesReceivedByClient.toString());
        Assert.assertEquals(messagesReceivedByServer, clientMessages, messagesReceivedByServer.toString());
    }

    @Test
    public void testSingleSyncConnection() {
        List<String> messages = Arrays.asList("asdf", "asdf5785fds", "SADD&D^&AS97898");
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncProducerMock syncProducer = new SyncProducerMock(encodingService, messages);

        try (SocketClient client = createSyncClient(syncProducer)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> expectedResponses = Arrays.asList("server -> asdf", "server -> asdf5785fds", "server -> SADD&D^&AS97898");
        List<String> responses = syncProducer.getReceivedMessages();
        Assert.assertEquals(responses, expectedResponses, responses.toString());
    }

    @Test
    public void testReconnection() {
        List<String> client1Messages = Arrays.asList("Data", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock client1Producer = createProducer(client1Messages);
        ReceiverMock client1Receiver = createReceiver();

        List<String> client2Messages = Arrays.asList("Another", "_set_", "*/*&of&&%", "DATA_))00");
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncProducerMock syncProducer = new SyncProducerMock(encodingService, client2Messages);

        try (SocketClient client = createAsyncClient(client1Producer, client1Receiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        try (SocketClient client = createSyncClient(syncProducer)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> messagesReceivedByClient1 = client1Receiver.getReceivedMessages();
        List<String> messagesReceivedByClient2 = syncProducer.getReceivedMessages();
        List<String> asyncMessagesReceivedByServer = serverReceiver.getReceivedMessages();
        List<String> expectedSyncResponses = Arrays.asList("server -> Another", "server -> _set_", "server -> */*&of&&%", "server -> DATA_))00");

        Assert.assertEquals(messagesReceivedByClient1, serverMessages, messagesReceivedByClient1.toString());
        Assert.assertEquals(messagesReceivedByClient2, expectedSyncResponses, messagesReceivedByClient2.toString());
        Assert.assertEquals(asyncMessagesReceivedByServer, client1Messages, asyncMessagesReceivedByServer.toString());
    }

    @Test
    public void testSyncDisconnection() {
        List<String> messages = Arrays.asList("asdf", "asdf5785fds", IntegrationTestsConfig.CLOSE_SIGNAL, "DDSD");
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncProducerMock syncProducer = new SyncProducerMock(encodingService, messages);

        try (SocketClient client = createSyncClient(syncProducer)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> expectedResponses = Arrays.asList("server -> asdf", "server -> asdf5785fds");
        List<String> responses = syncProducer.getReceivedMessages();
        Assert.assertEquals(responses, expectedResponses, responses.toString());
    }

    @Test
    public void testAsyncDisconnection() {
        List<String> clientMessages = Arrays.asList("Data", "from client", IntegrationTestsConfig.CLOSE_SIGNAL, "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createAsyncClient(clientProducer, clientReceiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();
        List<String> expectedServerMessages = Arrays.asList("Data", "from client", IntegrationTestsConfig.CLOSE_SIGNAL);

        Assert.assertEquals(messagesReceivedByServer, expectedServerMessages, messagesReceivedByServer.toString());
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
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncReceiverMock syncReceiver = new SyncReceiverMock(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createMixServer(() -> producer, () -> receiver, () -> syncReceiver);
    }

    private SocketClient createSyncClient(SyncProducer syncProducer) {
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createSyncClient(syncProducer, true);
    }

    private SocketClient createAsyncClient(Producer producer, Receiver receiver) {
        AesEncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createAsyncClient(producer, receiver, true);
    }
}
