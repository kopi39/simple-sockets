package org.kopi.socket.general.wrap;

public class Envelope {

    private final byte[] header;
    private final byte[] body;

    public Envelope(byte[] header, byte[] body) {
        if (header == null) {
            throw new IllegalArgumentException("Header cannot be null");
        }
        this.header = header;
        this.body = body;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

}
