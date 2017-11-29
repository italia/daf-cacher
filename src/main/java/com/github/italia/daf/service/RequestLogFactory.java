package com.github.italia.daf.service;

import org.eclipse.jetty.server.AbstractNCSARequestLog;

import java.io.IOException;
import java.util.logging.Logger;

public class RequestLogFactory {

    private Logger logger;

    public RequestLogFactory(Logger logger) {
        this.logger = logger;
    }

    AbstractNCSARequestLog create() {
        return new AbstractNCSARequestLog() {
            @Override
            protected boolean isEnabled() {
                return true;
            }

            @Override
            public void write(String s) throws IOException {
                logger.info(s);
            }
        };
    }
}
