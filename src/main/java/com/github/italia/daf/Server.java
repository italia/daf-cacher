package com.github.italia.daf;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Base64;

import static spark.Spark.*;
public class Server {

    public static void main(String[] args) {
        final JedisPool pool = new JedisPool(new JedisPoolConfig(), "localhost");
        get("/plot/:id/:geometry", (request, response) -> {
            response.type("image/png");
            String buffer = null;
            String geometry = request.params(":geometry") == null ? "original" : request.params(":geometry");
            try (Jedis jedis = pool.getResource()) {
                String key = "metabase-cacher:keys:" + request.params(":id") + ":" + geometry;
                buffer = jedis.get(key);
            }
            if (buffer != null) {
                return Base64.getDecoder().decode(buffer);
            }

            response.status(404);
            return null;

        });
    }
}
