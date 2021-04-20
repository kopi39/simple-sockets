package org.kopi.socket.tcp.proxy.itf;

import java.util.Optional;

public interface Interceptor {

    Optional<byte[]> toServer(byte[] fromClient);

    Optional<byte[]> toClient(byte[] fromServer);

    default int toServerIndex() {
        return 0;
    }

    default int toClientIndex() {
        return 0;
    }

}
