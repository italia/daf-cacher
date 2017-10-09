package com.github.italia.daf;

import com.github.italia.daf.metabase.HTTPClient;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Seeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWorker.class.getName());

    public static void main(String[] args) throws IOException {

        final Properties properties = new Configuration(args[0]).load();

        try (final Jedis jedis = new Jedis(properties.getProperty("caching.redis_host"))) {

            final HTTPClient client = new HTTPClient(
                    new URL(properties.getProperty("metabase.api_endpoint")),
                    new HTTPClient.Token(properties.getProperty("metabase.api_token")));

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    LOGGER.info("Fetch all public cards");
                    try {
                        for (final HTTPClient.Card card : client.getPublicCards()) {
                            LOGGER.info("Card id " + card.public_uuid + " enqueued for caching");
                            jedis.lpush("metabase-cacher:jobs", card.public_uuid);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "an exception was thrown", e);
                    }
                    LOGGER.info("Sleeping until the next iteration");
                }
            }, 0, Long.parseLong(properties.getProperty("caching.refresh_every")) * 1000 * 60);
        }
    }
}
