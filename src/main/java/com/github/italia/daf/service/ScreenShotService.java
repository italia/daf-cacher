package com.github.italia.daf.service;

import com.github.italia.daf.sniper.Page;
import com.github.italia.daf.sniper.PageSniper;
import com.github.italia.daf.utils.Geometry;
import com.github.italia.daf.utils.LoggerFactory;
import com.github.italia.daf.utils.Resize;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenShotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenShotService.class.getName());
    public static final String REDIS_NS = "daf-cacher:keys:";
    private Jedis jedis;
    private WebDriver webDriver;
    private URL url;
    private int ttl;
    private List<Geometry> thumbs;
    private String id;
    private int timeOutInSecond;
    private PageSniper pageSniper;
    private Page pageHandler;

    private ScreenShotService() {
        timeOutInSecond = 5;
    }

    public void perform() throws IOException, TimeoutException {

        final byte[] payload = pageSniper.shoot(this.url.toString());
        final String originalBase64Encoded = Base64.getEncoder().encodeToString(payload);
        final String redisKey = REDIS_NS + id + ":original";
        jedis.setex(redisKey, ttl * 60, originalBase64Encoded);

        for (final Geometry g : thumbs) {
            LOGGER.log(Level.INFO, () -> "Generate thumb of size " + g);
            final byte[] thumb = new Resize(payload).to(g);
            final String k = REDIS_NS + id + ":" + g;
            jedis.setex(k, ttl * 60, Base64.getEncoder().encodeToString(thumb));
        }
    }

    public byte[] fetch(String id, String size) {
        final String redisKey = REDIS_NS + id + ":" + size;
        final String payload = jedis.get(redisKey);

        if (payload != null && payload.length() > 0) {
            return Base64.getDecoder().decode(payload);
        }
        return new byte[0];
    }


    public static class Builder {
        private ScreenShotService service;

        public Builder() {
            this.service = new ScreenShotService();
            this.service.thumbs = new ArrayList<>();
        }

        public Builder webDriver(final WebDriver driver) {
            this.service.webDriver = driver;
            return this;
        }

        public Builder jedis(final Jedis jedis) {
            this.service.jedis = jedis;
            return this;
        }

        public Builder plotUrl(final URL url) {
            this.service.url = url;
            return this;
        }

        public Builder id(final String id) {
            this.service.id = id;
            return this;
        }

        public Builder ttl(final int ttl) {
            this.service.ttl = ttl;
            return this;
        }

        public Builder geometries(final List<Geometry> g) {
            this.service.thumbs.addAll(g);
            return this;
        }

        public Builder geometry(final Geometry g) {
            this.service.thumbs.add(g);
            return this;
        }

        public Builder timeout(int timeoutInSecond) {
            this.service.timeOutInSecond = timeoutInSecond;
            return this;
        }

        public Builder setPageHandler(Page handler) {
            this.service.pageHandler = handler;
            return this;
        }


        public ScreenShotService build() {
            this.service.pageSniper = new PageSniper(this.service.webDriver, this.service.timeOutInSecond);
            this.service.pageSniper.setPageHandler(this.service.pageHandler);
            return this.service;
        }

    }
}
