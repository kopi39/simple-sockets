package org.kopi.socket.tcp.strategies.mix;

import org.kopi.socket.tcp.strategies.async.AsyncStrategy;
import org.kopi.socket.tcp.strategies.async.TcpAsyncClient;

import java.io.OutputStream;
import java.net.Socket;

public class TcpMixAsyncClient extends TcpAsyncClient {

    public TcpMixAsyncClient(AsyncStrategy asyncStrategy) {
        super(asyncStrategy);
    }

    @Override
    protected void onConnect(Socket clientSocket) throws Exception {
        super.onConnect(clientSocket);
        OutputStream out = clientSocket.getOutputStream();
        out.write(AsyncStrategy.CODE);
        out.flush();
    }
}
