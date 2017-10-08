package com.github.italia.daf;

import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.util.Configuration;
import com.github.italia.daf.util.LoggerFactory;
import com.sun.xml.internal.rngom.parse.host.Base;
import net.coobird.thumbnailator.Thumbnails;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

        final List<PlotSniper.Geometry> sizes = new ArrayList<>();
        sizes.add(new PlotSniper.Geometry(100, 100));
        sizes.add(new PlotSniper.Geometry(400, 400));
        sizes.add(new PlotSniper.Geometry(250, 250));
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
            final byte[] payload = sniper.shootAsByte(url);
            final String originalBase64Encoded = Base64.getEncoder().encodeToString(payload);
            final String redisKey = "metabase-cacher:keys:" + id + ":original";
            jedis.setex(redisKey, Integer.parseInt(properties.getProperty("caching.ttl")) * 60, originalBase64Encoded);

            for (final PlotSniper.Geometry g: sizes) {
                LOGGER.info("Generate thumb of " + g);
                final byte[] thumb = new PlotSniper.Resize(payload).to(g);
                String k = "metabase-cacher:keys:" + id + ":" + g;
                jedis.setex(k,
                        Integer.parseInt(properties.getProperty("caching.ttl")) * 60,
                        Base64.getEncoder().encodeToString(thumb)
                );
            }

            LOGGER.info(url + " processed");
            jedis.lrem("metabase-cacher:jobsbq",1, id);
        } while (true);

        jedis.close();
    }
}
