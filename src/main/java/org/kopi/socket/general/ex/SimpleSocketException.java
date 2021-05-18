package org.kopi.socket.general.ex;

public class SimpleSocketException extends RuntimeException {

    public SimpleSocketException(Exception ex) {
        super(ex);
    }

    public SimpleSocketException(String message, Object... args) {
        super(String.format(message, args));
    }
}
