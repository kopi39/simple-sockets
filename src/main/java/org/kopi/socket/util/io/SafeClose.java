package org.kopi.socket.util.io;

import org.kopi.socket.general.ex.SimpleSocketException;

import java.io.Closeable;
import java.io.IOException;

public class SafeClose {

    public static void close(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException ex) {
            throw new SimpleSocketException(ex);
        }
    }

}
