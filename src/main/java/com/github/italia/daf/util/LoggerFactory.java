package com.github.italia.daf.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerFactory {
    static {
        try (InputStream stream = LoggerFactory.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger(final String className) {
        return Logger.getLogger(className);
    }
}
