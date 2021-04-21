package org.kopi.socket.integration.tcp.async;

import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.integration.tcp.general.ProducerMock;
import org.kopi.socket.integration.tcp.general.ReceiverMock;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.strategies.async.itf.Producer;
import org.kopi.socket.tcp.strategies.async.itf.Receiver;
import org.kopi.util.async.Async;
import org.kopi.util.encoding.Utf8EncodingService;
import org.kopi.util.io.SafeClose;
import org.kopi.util.security.AesEncryptionService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TcpAsyncSocketIntegrationTests {

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
    public void testSingleConnection() {
        List<String> clientMessages = Arrays.asList("Data", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createClient(clientProducer, clientReceiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> messagesReceivedByClient = clientReceiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();

        Assert.assertEquals(messagesReceivedByClient, serverMessages, messagesReceivedByClient.toString());
        Assert.assertEquals(messagesReceivedByServer, clientMessages, messagesReceivedByServer.toString());
    }

    @Test
    public void testReconnection() {
        List<String> client1Messages = Arrays.asList("Data", "from client", "TO", "server*/*-/$^%#$");
        ProducerMock client1Producer = createProducer(client1Messages);
        ReceiverMock client1Receiver = createReceiver();

        List<String> client2Messages = Arrays.asList("Another", "_set_", "*/*&of&&%", "DATA_))00");
        ProducerMock client2Producer = createProducer(client2Messages);
        ReceiverMock client2Receiver = createReceiver();

        try (SocketClient client = createClient(client1Producer, client1Receiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        try (SocketClient client = createClient(client2Producer, client2Receiver)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> messagesReceivedByClient1 = client1Receiver.getReceivedMessages();
        List<String> messagesReceivedByClient2 = client2Receiver.getReceivedMessages();
        List<String> messagesReceivedByServer = serverReceiver.getReceivedMessages();
        List<String> clientMessages = Stream.concat(client1Messages.stream(), client2Messages.stream()).collect(Collectors.toList());

        Assert.assertEquals(messagesReceivedByClient1, serverMessages, messagesReceivedByClient1.toString());
        Assert.assertEquals(messagesReceivedByClient2, serverMessages, messagesReceivedByClient2.toString());
        Assert.assertEquals(messagesReceivedByServer, clientMessages, messagesReceivedByServer.toString());
    }

    @Test
    public void testDisconnection() {
        List<String> clientMessages = Arrays.asList("Data", "from client", IntegrationTestsConfig.CLOSE_SIGNAL, "server*/*-/$^%#$");
        ProducerMock clientProducer = createProducer(clientMessages);
        ReceiverMock clientReceiver = createReceiver();

        try (SocketClient client = createClient(clientProducer, clientReceiver)) {
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
        AesEncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createAsyncServer(() -> producer, () -> receiver);
    }

    private SocketClient createClient(Producer producer, Receiver receiver) {
        AesEncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createAsyncClient(producer, receiver, false);
    }

}
