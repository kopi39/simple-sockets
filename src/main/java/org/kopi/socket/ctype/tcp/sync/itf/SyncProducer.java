package org.kopi.socket.ctype.tcp.sync.itf;

import org.kopi.socket.itf.SocketStrategy;

@FunctionalInterface
public interface SyncProducer {

    SocketStrategy.Result process(Sender sender);
}
