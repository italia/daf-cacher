package com.github.italia.daf;

import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.util.LoggerFactory;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CacheWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger( CacheWorker.class.getName() );
    public static void main(String[] args) throws IOException {
        final Jedis jedis = new Jedis(System.getenv("REDIS_HOST"));
        final WebDriver webDriver = new Browser
                .Builder(new URL("http://localhost:4444/wd/hub"))
                .chrome()
                .build()
                .webDriver();
        final PlotSniper sniper = new PlotSniper(webDriver);

        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        do {

            final String url = jedis.brpoplpush("metabase-cacher:jobs", "metabase-cacher:jobsbq", 10);
            if (url == null)
                continue;

            if (url.equals("EXIT")){
                LOGGER.info("Magic word received ... exiting from the main loop");
                break;
            }

            LOGGER.info("Processing url " + url);
            final String payload = sniper.shootAsBase64(url);
            final String redisKey = "metabase-cacher:keys:" + url;
            jedis.set(redisKey, payload);
            LOGGER.info( url + " processed");
            jedis.lrem("metabase-cacher:jobsbq",1, redisKey);
        } while (true);
        jedis.close();
    }
}
