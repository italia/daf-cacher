package com.github.italia.daf;

import com.github.italia.daf.metabase.HTTPClient;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

public class Seeder {
    private static final Logger LOGGER = LoggerFactory.getLogger( CacheWorker.class.getName() );
    public static void main(String[] args) throws IOException {
        final Properties properties = new Configuration(args[0]).load();
        final Jedis jedis = new Jedis(properties.getProperty("caching.redis_host"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> jedis.close()));

        HTTPClient client = new HTTPClient(
                new URL(properties.getProperty("metabase.api_endpoint")),
                new HTTPClient.Token(properties.getProperty("metabase.api_token")));

        do {
            LOGGER.info("Fetch all public cards");
            for (final HTTPClient.Card card : client.getPublicCards()) {
                LOGGER.info("Card id " + card.public_uuid + " enqueued for caching");
                jedis.lpush("metabase-cacher:jobs", card.public_uuid);
            }

            try {
                LOGGER.info("Sleeping until the next iteration");
                Thread.sleep(Integer.parseInt(properties.getProperty("caching.refresh_every")) * 1000 * 60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (true);

    }
}
