package org.kopi.socket.examples.tcp.proxy.single;

import org.kopi.socket.examples.config.Config;
import org.kopi.socket.examples.tcp.general.SimpleInterceptor;
import org.kopi.socket.itf.SocketServer;
import org.kopi.socket.tcp.general.TcpSocketFactory;
import org.kopi.socket.tcp.proxy.itf.Interceptor;
import org.kopi.util.encoding.Utf8EncodingService;

public class Proxy {

    public static void main(String[] args) {
        Utf8EncodingService encodingService = new Utf8EncodingService();
        Interceptor interceptor = new SimpleInterceptor(encodingService);
        TcpSocketFactory socketFactory = new TcpSocketFactory();

        try (SocketServer proxyServer = socketFactory.createProxy(Config.HOST, Config.PORT, interceptor)) {
            proxyServer.start(Config.PROXY_PORT);
        }
    }
}
