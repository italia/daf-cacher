package com.github.italia.daf;

import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.service.ScreenShotService;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.LoggerFactory;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWorker.class.getName());

    public static void main(String[] args) throws IOException {

        final Properties properties = new Configuration(args[0]).load();

        try (final Jedis jedis = new Jedis(properties.getProperty("caching.redis_host"))) {

            final WebDriver webDriver = new Browser
                    .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                    .chrome()
                    .build()
                    .webDriver();
            webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

            final String metabaseHost = properties.getProperty("metabase.host");


            final List<PlotSniper.Geometry> sizes = new ArrayList<>();
            for (final String token : properties.getProperty("caching.geometries").split("\\s+")) {
                sizes.add(PlotSniper.Geometry.fromString(token));
            }

            do {

                final String id = jedis.brpoplpush("metabase-cacher:jobs", "metabase-cacher:jobsbq", 10);
                if (id == null)
                    continue;

                if (id.equals("EXIT")) {
                    LOGGER.log(Level.INFO, () -> "Magic word received ... exiting from the main loop");
                    break;
                }

                final String url = metabaseHost + "/public/question/" + id;
                LOGGER.log(Level.INFO, () -> "Processing url " + url);

                ScreenShotService service = new ScreenShotService.Builder()
                        .id(id)
                        .jedis(jedis)
                        .webDriver(webDriver)
                        .plotUrl(new URL(url))
                        .ttl(Integer.parseInt(properties.getProperty("caching.ttl")))
                        .geometries(sizes)
                        .build();

                service.perform();

                LOGGER.log(Level.INFO, () -> url + " processed");
                jedis.lrem("metabase-cacher:jobsbq", 1, id);
            } while (true);
        }

    }
}
