package com.github.italia.daf;

import com.github.italia.daf.service.ApiService;
import com.github.italia.daf.util.Configuration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

public class Server {

    public static void main(String[] args) throws IOException, URISyntaxException {
        final Properties properties = new Configuration(args[0]).load();
        final ApiService api = new ApiService(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(api::stop));
        api.start();
    }
}
