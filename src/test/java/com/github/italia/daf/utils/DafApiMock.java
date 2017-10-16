package com.github.italia.daf.utils;

import com.github.italia.daf.dafapi.HTTPClient;
import com.google.gson.GsonBuilder;
import spark.Spark;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static spark.Spark.*;

public class DafApiMock {
    private Properties properties;
    private List<HTTPClient.EmbeddableData> fakeList;

    public DafApiMock(final Properties properties) {
        this.properties = properties;
        this.fakeList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            final HTTPClient.EmbeddableData d = new HTTPClient.EmbeddableData();
            d.setIdentifier("identifier_" + i);
            d.setOrigin(i % 2 == 0 ? "metabase" : "superset");
            d.setTitle("title_" + i);
            d.setIframeUrl("//iframe_url/" + i);
            this.fakeList.add(d);

        }
    }

    public void start() {
        port(6767);
        handleLogin();
        handleDataList();
        awaitInitialization();
    }

    public void stop() {
        Spark.stop();
    }

    private void handleLogin() {

        get("/security-manager/v1/token", ((request, response) -> {
            if (request.headers("Authorization") == null) {
                halt(403, "Request denied");
            }
            return "\"AAAAAAAAAA\"";
        }));
    }

    private void handleDataList() {

        get("/dati-gov/v1/dashboard/iframes", ((request, response) -> {
            if (request.headers("Authorization") == null) {
                halt(403, "Request denied");
            }
            response.type("application/json");
            return new GsonBuilder()
                    .create()
                    .toJson(this.fakeList);
        }));
    }
}
