package com.github.italia.daf.service;

import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.utils.Credential;
import com.github.italia.daf.utils.Geometry;
import com.github.italia.daf.utils.LoggerFactory;
import com.github.italia.daf.utils.Resize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import spark.Service;
import spark.Spark;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

public class ApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiService.class.getName());
    private static final String ORIGINAL_SIZE = "original";
    private Properties properties;
    private ThreadLocalWebDriver localWebDriver;
    private JedisPool jedisPool;
    private Service sparkService;


    public ApiService(final Properties properties) throws URISyntaxException {
        this.properties = properties;
        this.localWebDriver = new ThreadLocalWebDriver();
        this.jedisPool = new JedisPool(new JedisPoolConfig(), new URI(properties.getProperty("caching.redis_host")));
    }

    public void start() {
        sparkService = Service.ignite();
        sparkService.staticFiles.location("/public");
        handlePlotList();
        handlePlot();
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
                String key = "daf-cacher:keys:" + request.params(":id") + ":" + geometry;
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

                String key = "daf-cacher:keys:" + request.params(":id") + ":" + ORIGINAL_SIZE;
                buffer = jedis.get(key);

                // Cache is completely empty for this plot let's take a fresh snap
                if (buffer == null || buffer.length() == 0) {
                    response.status(404);
                    return null;
                } else {
                    decoded = Base64.getDecoder().decode(buffer);
                }
            }

            // A new size requested?
            if (!geometry.equals("original")) {
                decoded = new Resize(decoded)
                        .to(Geometry.fromString(geometry));
            }
            response.type("image/png");
            return decoded;
        });

    }

    private void handlePlotList() {
        sparkService.get("/plot/", (request, response) -> {

            Credential credential = new Credential(
                    properties.getProperty("daf_api.user"),
                    properties.getProperty("daf_api.password")
            );

            final com.github.italia.daf.dafapi.HTTPClient client = new com.github.italia.daf.dafapi.HTTPClient(new URL(properties.getProperty("daf_api.host")), credential);
            client.authenticate();
            final Gson gson = new GsonBuilder().create();
            response.type("application/json");
            return gson.toJson(client.getEmbeddableDataList());
        });
    }

    private final class ThreadLocalWebDriver extends ThreadLocal<WebDriver> {

        @Override
        protected WebDriver initialValue() {
            final WebDriver webDriver;
            try {
                webDriver = new Browser
                        .Builder(new URL(properties.getProperty("caching.selenium_hub")))
                        .chrome()
                        .build()
                        .webDriver();
                webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
                return webDriver;
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, "an exception was thrown", e);

            }
            return null;
        }

        @Override
        public void remove() {
            WebDriver driver = get();
            if (driver != null)
                driver.close();
            super.remove();
        }

        @Override
        public void set(WebDriver value) {
            throw new UnsupportedOperationException();
        }

    }
}
