package com.github.italia.daf;

import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.LoggerFactory;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CacheWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger( CacheWorker.class.getName() );

    public static void main(String[] args) throws IOException {
        final Properties properties = new Configuration(args[0]).load();
        final Jedis jedis = new Jedis(properties.getProperty("caching.redis_host"));
        final WebDriver webDriver = new Browser
                .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                .chrome()
                .build()
                .webDriver();
        final PlotSniper sniper = new PlotSniper(webDriver);

        final String metabaseHost = properties.getProperty("metabase.host");

        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        do {

            final String id = jedis.brpoplpush("metabase-cacher:jobs", "metabase-cacher:jobsbq", 10);
            if (id == null)
                continue;

            if (id.equals("EXIT")){
                LOGGER.info("Magic word received ... exiting from the main loop");
                break;
            }

            final String url = metabaseHost + "/public/question/" + id;

            LOGGER.info("Processing url " + url);
            final String payload = sniper.shootAsBase64(url);
            final String redisKey = "metabase-cacher:keys:" + id;
            jedis.setex(redisKey, Integer.parseInt(properties.getProperty("caching.ttl")), payload);
            LOGGER.info( url + " processed");
            jedis.lrem("metabase-cacher:jobsbq",1, id);
        } while (true);
        jedis.close();
    }
}
