package org.kopi.web.tcp.sync.logic.itf;

import org.kopi.web.tcp.sync.logic.model.Response;

@FunctionalInterface
public interface Interpreter {
    Response process(byte[] input);
}
