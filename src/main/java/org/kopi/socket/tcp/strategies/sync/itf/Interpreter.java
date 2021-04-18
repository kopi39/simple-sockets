package org.kopi.socket.tcp.strategies.sync.itf;


@FunctionalInterface
public interface Interpreter {
    Response process(byte[] input);

    class Response {
        private boolean closeServer;
        private byte[] body;

        public boolean isCloseServer() {
            return closeServer;
        }

        public byte[] getBody() {
            return body;
        }

        public static Response closeServer() {
            Response response = new Response();
            response.closeServer = true;
            return response;
        }

        public static Response create(byte[] body) {
            Response response = new Response();
            response.body = body;
            return response;
        }
    }
}
