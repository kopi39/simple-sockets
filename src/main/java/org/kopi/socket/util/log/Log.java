package org.kopi.socket.util.log;

import java.util.logging.Logger;

public class Log {

    public static final String NAME = "simple-sockets";

    public static Logger get() {
        return Logger.getLogger(NAME);
    }

}
