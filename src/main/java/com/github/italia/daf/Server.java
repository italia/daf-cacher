package com.github.italia.daf;
import com.github.italia.daf.util.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

import static spark.Spark.*;
public class Server {

    public static void main(String[] args) throws IOException {
        final Properties properties = new Configuration(args[0]).load();
        final JedisPool pool = new JedisPool(new JedisPoolConfig(), properties.getProperty("caching.redis_host"));

        staticFiles.location("/public");

        get("/plot/:id/:geometry", (request, response) -> {

            String buffer;
            String geometry = request.params(":geometry") == null ? "original" : request.params(":geometry");
            try (Jedis jedis = pool.getResource()) {
                String key = "metabase-cacher:keys:" + request.params(":id") + ":" + geometry;
                buffer = jedis.get(key);
            }
            if (buffer != null) {
                response.type("image/png");
                return Base64.getDecoder().decode(buffer);
            }

            response.status(404);
            return null;
        });
    }
}
