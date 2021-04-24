package org.kopi.socket.util.encoding.itf;

public interface EncodingService<I, O> {

    O encode(I input);

    I decode(O output);

}
