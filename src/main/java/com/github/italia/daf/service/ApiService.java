package com.github.italia.daf.service;

import com.github.italia.daf.dafapi.HTTPClient;
import com.github.italia.daf.data.EmbeddableData;
import com.github.italia.daf.metabase.MetabaseSniperPageImpl;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.superset.SupersetSniperPageImpl;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.Geometry;
import com.github.italia.daf.utils.LoggerFactory;
import com.github.italia.daf.utils.Resize;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import spark.Request;
import spark.Response;
import spark.Service;
import spark.embeddedserver.EmbeddedServers;
import spark.embeddedserver.jetty.EmbeddedJettyFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.italia.daf.service.ScreenShotService.REDIS_NS;

public class ApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiService.class.getName());
    private static final String ORIGINAL_SIZE = "original";
    private Properties properties;

    private JedisPool jedisPool;
    private Service sparkService;
    private Credential supersetCredential;
    private Credential dafApiCredential;
    private AbstractNCSARequestLog requestLog;


    public ApiService(final Properties properties) throws URISyntaxException {
        this.properties = properties;
        this.requestLog = new RequestLogFactory(LOGGER).create();
        EmbeddedJettyFactory factory = new EmbeddedJettyFactoryConstructor(requestLog).create();
        EmbeddedServers.add(EmbeddedServers.Identifiers.JETTY, factory);
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128); // maximum active connections
        poolConfig.setMaxIdle(32);  // maximum idle connections

        this.jedisPool = new JedisPool(poolConfig, new URI(properties.getProperty("caching.redis_host")));
        this.supersetCredential = new Credential(
                properties.getProperty("superset.user"),
                properties.getProperty("superset.password")
        );

        this.dafApiCredential = new Credential(
                properties.getProperty("daf_api.user"),
                properties.getProperty("daf_api.password")
        );

    }

    public void start() {

        sparkService = Service
                .ignite()
                .threadPool(32, 2, 60000);
        sparkService.staticFiles.location("/public");
        handlePlotList();
        handlePlot();
        handleStatus();

        sparkService.awaitInitialization();

    }

    public void stop() {
        sparkService.stop();
        jedisPool.close();
        jedisPool.destroy();

    }

    private void handlePlot() {
        sparkService.get("/plot/:id/:geometry", (request, response) -> {

            String buffer;
            String geometry = request.params(":geometry");

            // Parameter check
            if (!geometry.equalsIgnoreCase(ORIGINAL_SIZE)) {
                try {
                    Geometry.fromString(geometry);
                } catch (NumberFormatException e) {
                    response.status(404);
                    return null;
                }
            }

            try (Jedis jedis = jedisPool.getResource()) {
                String key = REDIS_NS + request.params(":id") + ":" + geometry;
                buffer = jedis.get(key);
            }

            // We have a cache hit
            if (buffer != null) {
                response.type("image/png");
                return Base64.getDecoder().decode(buffer);
            }

            byte[] decoded;
            // Cache miss. Let's see if an original size is available
            try (Jedis jedis = jedisPool.getResource()) {

                String key = REDIS_NS + request.params(":id") + ":" + ORIGINAL_SIZE;
                buffer = jedis.get(key);

                // Cache is completely empty for this plot let's take a fresh snap
                if (buffer == null || buffer.length() == 0) {
                    decoded = handleNewSnap(request.params(":id"), ORIGINAL_SIZE);
                } else {
                    decoded = Base64.getDecoder().decode(buffer);
                }
            }

            // A new size requested?
            if (!geometry.equals(ORIGINAL_SIZE)) {
                decoded = new Resize(decoded)
                        .to(Geometry.fromString(geometry));
            }
            response.type("image/png");
            return decoded;
        });

    }

    private void handlePlotList() {
        sparkService.get("/plot/", (request, response) -> {
            final Gson gson = new GsonBuilder().create();
            response.type("application/json");
            return gson.toJson(getAvailablePlotList());
        });
    }

    private void handleStatus() {
        sparkService.get("/status", (Request request, Response response) -> "OK");
    }

    private byte[] handleNewSnap(final String id, final String size) throws IOException, TimeoutException {

        final EmbeddableData data = getAvailablePlotList()
                .stream()
                .filter(x -> x.getIdentifier().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The request " + id + " is not available"));
        final WebDriver webDriver = webDriver();
        try (Jedis jedis = jedisPool.getResource()) {
            final Page pageHandler = fromId(id);

            final ScreenShotService.Builder builder = new ScreenShotService
                    .Builder()
                    .setPageHandler(pageHandler)
                    .jedis(jedis)
                    .id(id)
                    .webDriver(webDriver)
                    .plotUrl(new URL(data.getIframeUrl()))
                    .ttl(Integer.parseInt(properties.getProperty("caching.ttl")))
                    .timeout(30);

            if (!size.equals(ORIGINAL_SIZE))
                builder.geometry(Geometry.fromString(size));

            final ScreenShotService service = builder.build();
            service.perform();
            return service.fetch(id, size);
        } finally {
            try {
                if (webDriver != null) {
                    webDriver.close();
                    webDriver.quit();
                }
            } catch (Exception ex) {
                //Ignored
            }
        }


    }

    private Page fromId(final String id) {

        if (id.startsWith("metabase_") || id.startsWith("tdmetabase_")) {
            return new MetabaseSniperPageImpl(properties);
        }

        if (id.startsWith("superset_")) {
            try {
                return new SupersetSniperPageImpl
                        .Builder()
                        .setCredential(supersetCredential)
                        .setSupersetLoginUrl(new URL(properties.getProperty("superset.login_url")))
                        .implicitWait(Integer.parseInt(properties.getProperty("caching.page_load_wait")))
                        .getSniperPage();
            } catch (MalformedURLException e) {
                /* ignored */
            }
        }

        throw new IllegalArgumentException("ID " + id + " not handled");
    }

    private List<EmbeddableData> getAvailablePlotList() throws IOException {
        try (Jedis jedis = jedisPool.getResource()) {

            final String cachedPayload = jedis.get(REDIS_NS + "available-list");

            if (cachedPayload == null) {
                final HTTPClient client = new HTTPClient(new URL(properties.getProperty("daf_api.host")), dafApiCredential);
                client.authenticate();
                final List<EmbeddableData> dataList = client.getList();
                jedis.setex(
                        REDIS_NS + "available-list",
                        Integer.parseInt(properties.getProperty("caching.ttl")) * 60,
                        new GsonBuilder().create().toJson(dataList)
                );
                return dataList;
            } else {
                return new GsonBuilder().create().fromJson(cachedPayload, new TypeToken<List<EmbeddableData>>() {
                }.getType());
            }
        }

    }

    private WebDriver webDriver() {
        try {
            return new Browser
                    .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                    .chrome()
                    .build()
                    .webDriver();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "an exception was thrown", e);

        }
        return null;
    }

}
