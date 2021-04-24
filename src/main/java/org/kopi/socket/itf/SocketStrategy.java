package org.kopi.socket.itf;

import java.io.Closeable;
import java.net.Socket;

public interface SocketStrategy extends AutoCloseable, Closeable {
    Result apply(Socket clientSocket);

    int getStrategyCode();

    void close();

    class Result {
        private final boolean stopServer;

        public Result(boolean stopServer) {
            this.stopServer = stopServer;
        }

        public static Result stopServer() {
            return new Result(true);
        }

        public static Result disconnectClient() {
            return new Result(false);
        }

        public boolean isStopServer() {
            return stopServer;
        }
    }
}
