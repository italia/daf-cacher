package com.github.italia.daf;

import com.github.italia.daf.data.EmbeddableData;
import com.github.italia.daf.metabase.MetabaseSniperPageImpl;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.service.ScreenShotService;
import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.superset.SupersetSniperPageImpl;
import com.github.italia.daf.utils.Configuration;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.Geometry;
import com.github.italia.daf.utils.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("squid:S135")
public class CacheWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWorker.class.getName());
    private static final Map<String, Page> PAGE_MAP = new HashMap<>();

    private static final String REDIS_Q = "daf-cacher:jobs";
    private static final String REDIS_BPQ = "daf-cacher:jobsbq";

    public static void main(String[] args) throws IOException, URISyntaxException, TimeoutException {

        final Properties properties = new Configuration(args[0]).load();

        final WebDriver webDriver = new Browser
                .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                .chrome()
                .build()
                .webDriver();
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            webDriver.close();
            webDriver.quit();
        }));

        final Page metabasePageHandler = new MetabaseSniperPageImpl();
        final Page supersetPageHandler = new SupersetSniperPageImpl
                .Builder()
                .setSupersetLoginUrl(new URL(properties.getProperty("superset.login_url")))
                .setCredential(new Credential(
                        properties.getProperty("superset.user"),
                        properties.getProperty("superset.password"))
                )
                .getSniperPage();

        PAGE_MAP.put("metabase", metabasePageHandler);
        PAGE_MAP.put("tdmetabase", metabasePageHandler);
        PAGE_MAP.put("superset", supersetPageHandler);


        try (final Jedis jedis = new Jedis(new URI(properties.getProperty("caching.redis_host")))) {
            final Gson gson = new GsonBuilder().create();
            final List<Geometry> sizes = new ArrayList<>();
            Arrays
                    .stream(properties.getProperty("caching.geometries").split("\\s+"))
                    .forEach(x -> sizes.add(Geometry.fromString(x)));

            do {
                final String embedPayload = jedis.brpoplpush(REDIS_Q, REDIS_BPQ, 10);

                if (embedPayload == null)
                    continue;

                if (embedPayload.equals("EXIT")) {
                    LOGGER.log(Level.INFO, () -> "Magic word received ... exiting from the main loop");
                    break;
                }

                EmbeddableData embeddableData = gson.fromJson(embedPayload, EmbeddableData.class);


                LOGGER.log(Level.INFO, () -> "Processing url " + embeddableData.getIframeUrl() + "[origin=" + embeddableData.getOrigin() + "]");

                final Page handler = PAGE_MAP.getOrDefault(embeddableData.getOrigin(), null);

                if (handler == null) {
                    LOGGER.log(Level.SEVERE, "Origin " + embeddableData.getOrigin() + " not supported");
                    jedis.lrem(REDIS_BPQ, 1, embeddableData.getIdentifier());
                    continue;
                }

                ScreenShotService service = new ScreenShotService.Builder()
                        .id(embeddableData.getIdentifier())
                        .jedis(jedis)
                        .webDriver(webDriver)
                        .plotUrl(new URL(embeddableData.getIframeUrl()))
                        .ttl(Integer.parseInt(properties.getProperty("caching.ttl")))
                        .geometries(sizes)
                        .timeout(30)
                        .setPageHandler(handler)
                        .build();

                service.perform();

                LOGGER.log(Level.INFO, () -> embeddableData.getIframeUrl() + " processed");
                jedis.lrem(REDIS_BPQ, 1, embeddableData.getIdentifier());
            } while (true);
        }

    }
}
