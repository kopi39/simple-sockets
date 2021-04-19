package org.kopi.socket.examples.tcp.proxy.multi;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.util.security.AesEncryptionService;
import org.kopi.util.security.itf.EncryptionService;

public class Proxy2 {

    public static void main(String[] args) {
        EncryptionService encryptionService = new AesEncryptionService(Config.TMP_KEY);
        TcpSocketFactory socketFactory = new TcpSocketFactory(encryptionService);
        try (SocketServer proxyServer = socketFactory.createClientProxy(Config.HOST, Config.PROXY_PORT)) {
            proxyServer.start(Config.PROXY_2_PORT);
        }
    }

}
