package com.github.italia.daf;

import com.github.italia.daf.metabase.HTTPClient;
import org.apache.commons.io.FileUtils;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Seeder {

    public static void main(String[] args) throws IOException {
        final Jedis jedis = new Jedis(System.getenv("REDIS_HOST"));
        HTTPClient client = new HTTPClient(new URL(System.getenv("METABASE_URL")), new HTTPClient.Credential(System.getenv("METABASE_USERNAME"), System.getenv("METABASE_PASSWORD")));
        client.authenticate();

        for (final HTTPClient.Card card : client.getPublicCards()) {
            final String url = "https://graph.daf.teamdigitale.it/public/question/" + card.public_uuid;
            jedis.lpush("metabase-cacher:jobs", url);
        }

        jedis.close();
    }
}
