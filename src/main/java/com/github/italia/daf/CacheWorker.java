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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"squid:S135", "squid:S3776"})
public class CacheWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheWorker.class.getName());
    private static final Map<String, Page> PAGE_MAP = new HashMap<>();

    private static final String REDIS_Q = "daf-cacher:jobs";
    private static final String REDIS_BPQ = "daf-cacher:jobsbq";

    private static final int REQUEST_RESET_AT = 10;

    private static WebDriver getWebDriver(final Properties properties, int timeout) throws MalformedURLException {
        WebDriver webDriver;
        while (true) {
            try {
                webDriver = new Browser
                        .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                        .chrome()
                        .build()
                        .webDriver();
                LOGGER.info("WebDriver is ready to rocks");
                return webDriver;
            } catch (org.openqa.selenium.WebDriverException we) {
                if (--timeout == 0) {
                    LOGGER.log(Level.SEVERE, "Cannot obtain stable connection with Selenium backend");
                    throw we;
                }
                try {
                    LOGGER.info("Selenium is not ready. Retrying in 1s. Retries left: " + timeout);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.warning("Interrupted!");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {

        final Properties properties = new Configuration(args[0]).load();
        int timeout = 30;
        int maxRequest = REQUEST_RESET_AT;

        final Page metabasePageHandler = new MetabaseSniperPageImpl(properties);
        final Page supersetPageHandler = new SupersetSniperPageImpl
                .Builder()
                .setSupersetLoginUrl(new URL(properties.getProperty("superset.login_url")))
                .setCredential(new Credential(
                        properties.getProperty("superset.user"),
                        properties.getProperty("superset.password"))
                )
                .implicitWait(Integer.parseInt(properties.getProperty("caching.page_load_wait")))
                .getSniperPage();

        PAGE_MAP.put("metabase", metabasePageHandler);
        PAGE_MAP.put("tdmetabase", metabasePageHandler);
        PAGE_MAP.put("superset", supersetPageHandler);


        try (final Jedis jedis = new Jedis(new URI(properties.getProperty("caching.redis_host")))) {
            WebDriver webDriver = getWebDriver(properties, timeout);
            final Gson gson = new GsonBuilder().create();
            final List<Geometry> sizes = new ArrayList<>();
            Arrays
                    .stream(properties.getProperty("caching.geometries").split("\\s+"))
                    .forEach(x -> sizes.add(Geometry.fromString(x)));

            do {
                final String embedPayload = jedis.brpoplpush(REDIS_Q, REDIS_BPQ, 10);

                if (embedPayload == null) {
                    maxRequest = 0;
                    continue;

                }

                if (embedPayload.equals("EXIT")) {
                    LOGGER.log(Level.INFO, () -> "Magic word received ... exiting from the main loop");
                    break;
                }

                EmbeddableData embeddableData = gson.fromJson(embedPayload, EmbeddableData.class);


                LOGGER.log(Level.INFO, () -> "Processing url " + embeddableData.getIframeUrl() + " [origin=" + embeddableData.getOrigin() + "]");

                final Page handler = PAGE_MAP.getOrDefault(embeddableData.getOrigin(), null);

                if (handler == null) {
                    LOGGER.log(Level.SEVERE, "Origin " + embeddableData.getOrigin() + " not supported");
                    jedis.lrem(REDIS_BPQ, 1, embeddableData.getIdentifier());
                    continue;
                }

                if (maxRequest == 0) {
                    LOGGER.log(Level.INFO, "Max number of request reached. Refreshing WebDriver");
                    try {
                        if (webDriver != null) {
                            webDriver.close();
                            webDriver.quit();
                        }
                    } catch (Exception e) {
                        // ignored
                    } finally {
                        maxRequest = REQUEST_RESET_AT;
                        ((SupersetSniperPageImpl) supersetPageHandler).reset();
                        webDriver = getWebDriver(properties, timeout);
                    }

                }


                try {
                    maxRequest--;
                    ScreenShotService service = new ScreenShotService.Builder()
                            .id(embeddableData.getIdentifier())
                            .jedis(jedis)
                            .webDriver(webDriver)
                            .plotUrl(new URL(embeddableData.getIframeUrl()))
                            .ttl(Integer.parseInt(properties.getProperty("caching.ttl")))
                            .geometries(sizes)
                            .timeout(Integer.parseInt(properties.getProperty("caching.selenium_timeout")))
                            .setPageHandler(handler)
                            .build();

                    service.perform();
                } catch (java.util.concurrent.TimeoutException tex) {
                    LOGGER.log(Level.SEVERE, "Origin " + embeddableData.getOrigin() + " timeout ex", tex);
                    LOGGER.log(Level.SEVERE, "WebDriver instance might be tainted. Refreshing it at the next iteration");
                    maxRequest = 0;
                    continue;
                }

                LOGGER.log(Level.INFO, () -> embeddableData.getIframeUrl() + " processed");
                jedis.lrem(REDIS_BPQ, 1, embedPayload);
            } while (true);
        }

    }
}
