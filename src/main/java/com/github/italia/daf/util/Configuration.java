package com.github.italia.daf.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private Properties properties;
    private String propFile;

    public Configuration(String propFile) {
        this.propFile = propFile;
        this.properties = new Properties();
    }

    public Properties load() throws IOException {
        try (FileInputStream in = new FileInputStream(propFile)) {
            properties.load(in);
        }
        return properties;
    }
}
