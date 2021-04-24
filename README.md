![build](https://github.com/kopi39/simple-sockets/actions/workflows/maven.yml/badge.svg)
![coverage](https://github.com/kopi39/simple-sockets/blob/master/.github/badges/jacoco.svg)

# simple-sockets
Simple implementation of communication patterns with java sockets. 
- Synchronous communication (client sends request to server and then waits for response)
- Asynchronous communication (client and server can send and receive data independently)
- Proxy
  - Proxy with interception
  - Proxy tunnel with encryption

Server allows to create 1-1 or 1-N connections.

## Examples
Simple example of synchronous client-server (1-1) communication with AES encryption.
All examples can be found [here](https://github.com/kopi39/simple-sockets/tree/master/src/examples/java/org/kopi/socket/examples/tcp)

### Client
```java
Utf8EncodingService encodingService = new Utf8EncodingService();
EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
SyncProducer syncProducer = new ConsoleSyncProducer(encodingService);
TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

try (SocketClient client = socketFactory.createSyncClient(syncProducer, false)) {
  client.connect(Config.HOST, Config.PORT);
}
```

### Server
```java
Utf8EncodingService encodingService = new Utf8EncodingService();
Supplier<SyncReceiver> syncReceiver = () -> new SimpleServerSyncReceiver(encodingService);
EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);

try (SocketServer server = socketFactory.createSyncServer(syncReceiver)) {
  server.start(Config.PORT);
}
```
