package org.kopi.socket.util.io;

import java.io.Closeable;

public class SafeClose {

    public static void close(Closeable... closeables) {
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
