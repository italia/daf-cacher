package com.github.italia.daf;

import com.github.italia.daf.dafapi.HTTPClient;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.Credential;
import com.github.italia.daf.util.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Seeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWorker.class.getName());

    public static void main(String[] args) throws IOException, URISyntaxException {

        final Properties properties = new Configuration(args[0]).load();
        Credential credential = new Credential(
                properties.getProperty("daf_api.user"),
                properties.getProperty("daf_api.password")
        );

        final HTTPClient client = new HTTPClient(new URL(properties.getProperty("daf_api.host")), credential);

        final URI redisURI = new URI(properties.getProperty("caching.redis_host"));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try (final Jedis jedis = new Jedis(redisURI)) {
                    LOGGER.info("Fetch all public cards");
                    final Gson gson = new GsonBuilder().create();
                    try {
                        client.authenticate();
                        for (final HTTPClient.EmbeddableData embeddableData : client.getEmbeddableDataList()) {
                            LOGGER.info("Card id " + embeddableData.getIdentifier() + " [" + embeddableData.getOrigin() + "] enqueued for caching");
                            jedis.lpush("daf-cacher:jobs", gson.toJson(embeddableData));
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "an exception was thrown", e);
                    }
                    LOGGER.info("Sleeping until the next iteration");
                }
            }
        }, 0, Long.parseLong(properties.getProperty("caching.refresh_every")) * 1000 * 60);
    }
}

