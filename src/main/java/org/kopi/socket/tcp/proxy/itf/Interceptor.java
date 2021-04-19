package org.kopi.socket.tcp.proxy.itf;

import java.util.Optional;

public interface Interceptor {

    Optional<byte[]> toServer(byte[] fromClient);

    Optional<byte[]> toClient(byte[] fromServer);

}
