package org.kopi.web.tcp.sync;

import org.kopi.config.Config;
import org.kopi.web.tcp.sync.logic.TcpSyncServer;

public class Server {

    public static void main(String[] args) {
        TcpSyncServer server = new TcpSyncServer();
        server.start(Config.PORT);
    }

}
