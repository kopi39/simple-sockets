package org.kopi.socket.integration.tcp.sync;

import org.kopi.socket.integration.config.IntegrationTestsConfig;
import org.kopi.socket.integration.tcp.general.SyncProducerMock;
import org.kopi.socket.integration.tcp.general.SyncReceiverMock;
import org.kopi.socket.itf.SocketClient;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.strategies.sync.itf.SyncProducer;
import org.kopi.socket.tcp.strategies.sync.itf.SyncReceiver;
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
import java.util.function.Supplier;

public class TcpSyncSocketIntegrationTests {

    private SocketServer server;
    private Thread serverThread;

    @BeforeMethod
    public void setUp() throws InterruptedException {
        this.server = createServer();
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
        List<String> messages = Arrays.asList("asdf", "asdf5785fds", "SADD&D^&AS97898");
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncProducerMock syncProducer = new SyncProducerMock(encodingService, messages);

        try (SocketClient client = createClient(syncProducer)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> expectedResponses = Arrays.asList("server -> asdf", "server -> asdf5785fds", "server -> SADD&D^&AS97898");
        List<String> responses = syncProducer.getReceivedMessages();
        Assert.assertEquals(responses, expectedResponses, responses.toString());
    }

    @Test
    public void testDisconnection() {
        List<String> messages = Arrays.asList("asdf", "asdf5785fds", IntegrationTestsConfig.CLOSE_SIGNAL, "DDSD");
        Utf8EncodingService encodingService = new Utf8EncodingService();
        SyncProducerMock syncProducer = new SyncProducerMock(encodingService, messages);

        try (SocketClient client = createClient(syncProducer)) {
            client.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> expectedResponses = Arrays.asList("server -> asdf", "server -> asdf5785fds");
        List<String> responses = syncProducer.getReceivedMessages();
        Assert.assertEquals(responses, expectedResponses, responses.toString());
    }

    @Test
    public void testReconnection() {
        Utf8EncodingService encodingService = new Utf8EncodingService();

        List<String> messages1 = Arrays.asList("asdf", "asdf5785fds", "SADD&D^&AS97898");
        List<String> messages2 = Arrays.asList("jud", "54/-*8/*", "0976faKJH.;", "7855674asdf");
        SyncProducerMock syncProducer1 = new SyncProducerMock(encodingService, messages1);
        SyncProducerMock syncProducer2 = new SyncProducerMock(encodingService, messages2);

        try (SocketClient client1 = createClient(syncProducer1)) {
            client1.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        try (SocketClient client2 = createClient(syncProducer2)) {
            client2.connect(IntegrationTestsConfig.HOST, IntegrationTestsConfig.PORT);
        }

        List<String> expectedResponses1 = Arrays.asList("server -> asdf", "server -> asdf5785fds", "server -> SADD&D^&AS97898");
        List<String> expectedResponses2 = Arrays.asList("server -> jud", "server -> 54/-*8/*", "server -> 0976faKJH.;", "server -> 7855674asdf");
        List<String> responses1 = syncProducer1.getReceivedMessages();
        List<String> responses2 = syncProducer2.getReceivedMessages();
        Assert.assertEquals(responses1, expectedResponses1, responses1.toString());
        Assert.assertEquals(responses2, expectedResponses2, responses2.toString());
    }

    private SocketServer createServer() {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        Supplier<SyncReceiver> syncReceiver = () -> new SyncReceiverMock(encodingService);
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createSyncServer(syncReceiver);
    }

    private SocketClient createClient(SyncProducer syncProducer) {
        EncryptionService encryptionService = new AesEncryptionService(IntegrationTestsConfig.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        return socketFactory.createSyncClient(syncProducer, false);
    }

}
