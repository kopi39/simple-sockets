package org.kopi.web.tcp.mix.logic;

import org.kopi.web.tcp.async.logic.AsyncStrategy;
import org.kopi.web.tcp.async.logic.TcpAsyncClient;

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
