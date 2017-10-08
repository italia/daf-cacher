package com.github.italia.daf;
import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.selenium.Browser;
import com.github.italia.daf.util.Configuration;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;
public class Server {


    public static void main(String[] args) throws IOException {
        final Properties properties = new Configuration(args[0]).load();
        final JedisPool pool = new JedisPool(new JedisPoolConfig(), properties.getProperty("caching.redis_host"));

        final ThreadLocal<WebDriver> webDriverLocal = new ThreadLocal<WebDriver>(){
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
                    e.printStackTrace();
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
        };



        staticFiles.location("/public");

        get("/plot/:id/:geometry", (request, response) -> {

            String buffer;
            String geometry = request.params(":geometry");

            try (Jedis jedis = pool.getResource()) {
                String key = "metabase-cacher:keys:" + request.params(":id") + ":" + geometry;
                buffer = jedis.get(key);
            }

            if (buffer != null) {
                response.type("image/png");
                return Base64.getDecoder().decode(buffer);
            }

            byte[] decoded;
            // Cache miss. Let's see if an original size is available
            try (Jedis jedis = pool.getResource()) {

                String key = "metabase-cacher:keys:" + request.params(":id") + ":original";
                buffer = jedis.get(key);


                if (buffer == null || buffer.length() == 0) { // Cache is completely empty for this plot let's take a fresh snap
                    final PlotSniper sniper = new PlotSniper(webDriverLocal.get());
                    final String metabaseHost = properties.getProperty("metabase.host");
                    final String url = metabaseHost + "/public/question/" + request.params(":id");

                    try {
                        decoded = sniper.shootAsByte(url);
                    } catch (Exception ex){
                        ex.printStackTrace();
                        response.status(404);
                        return null;
                    } finally {
                        webDriverLocal.remove();
                    }
                    // Then cache it
                    jedis.setex(key,
                            Integer.parseInt(properties.getProperty("caching.ttl")) * 60,
                            Base64.getEncoder().encodeToString(decoded)
                    );

                } else {
                    decoded = Base64.getDecoder().decode(buffer);
                }

            }

            if (!geometry.equals("original")) {
                decoded = new PlotSniper
                                .Resize(decoded)
                                .to(PlotSniper.Geometry.fromString(geometry));
            }
            response.type("image/png");
            return decoded;
        });
    }
}
