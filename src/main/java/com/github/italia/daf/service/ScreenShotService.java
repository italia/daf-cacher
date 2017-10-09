package com.github.italia.daf.service;

import com.github.italia.daf.metabase.PlotSniper;
import com.github.italia.daf.util.LoggerFactory;
import org.openqa.selenium.WebDriver;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScreenShotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenShotService.class.getName());
    private static final String REDIS_NS = "metabase-cacher:keys:";
    private Jedis jedis;
    private WebDriver webDriver;
    private URL url;
    private int ttl;
    private List<PlotSniper.Geometry> thumbs;
    private PlotSniper sniper;
    private String id;

    private ScreenShotService() {
    }

    public void perform() throws IOException {

        final byte[] payload = sniper().shootAsByte(url.toString());
        final String originalBase64Encoded = Base64.getEncoder().encodeToString(payload);
        final String redisKey = REDIS_NS + id + ":original";
        jedis.setex(redisKey, ttl * 60, originalBase64Encoded);

        for (final PlotSniper.Geometry g : thumbs) {
            LOGGER.log(Level.INFO, () -> "Generate thumb of size " + g);
            final byte[] thumb = new PlotSniper.Resize(payload).to(g);
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

    private PlotSniper sniper() {
        if (this.sniper == null)
            this.sniper = new PlotSniper(webDriver);

        return this.sniper;
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

        public Builder geometries(final List<PlotSniper.Geometry> g) {
            this.service.thumbs.addAll(g);
            return this;
        }

        public Builder geometry(final PlotSniper.Geometry g) {
            this.service.thumbs.add(g);
            return this;
        }


        public ScreenShotService build() {
            return this.service;
        }

    }
}
