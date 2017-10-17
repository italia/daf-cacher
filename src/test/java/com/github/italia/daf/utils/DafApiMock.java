package com.github.italia.daf.utils;

import com.google.gson.GsonBuilder;
import spark.Spark;

import java.util.Properties;

import static spark.Spark.*;

public class DafApiMock {
    private Properties properties;
    private DataProvider dataProvider;

    public DafApiMock(final Properties properties, DataProvider dataProvider) {
        this.properties = properties;
        this.dataProvider = dataProvider;
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
                    .toJson(this.dataProvider.getList());
        }));
    }
}
