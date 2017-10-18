package com.github.italia.daf.utils;

import com.github.italia.daf.service.ApiServiceTest;
import com.google.gson.GsonBuilder;
import spark.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DafApiMock {
    private Properties properties;
    private DataProvider dataProvider;
    private Service sparkService;

    public DafApiMock(final Properties properties, DataProvider dataProvider) {
        this.properties = properties;
        this.dataProvider = dataProvider;
    }

    public void start() {
        sparkService = Service.ignite();
        sparkService.port(6767);
        handleLogin();
        handleDataList();
        sparkService.awaitInitialization();
    }

    public void stop() {
        sparkService.stop();
    }

    private void handleLogin() {

        sparkService.get("/security-manager/v1/token", ((request, response) -> {
            if (request.headers("Authorization") == null) {
                sparkService.halt(403, "Request denied");
            }
            return "\"AAAAAAAAAA\"";
        }));
    }

    private void handleDataList() {

        sparkService.get("/dati-gov/v1/dashboard/iframes", ((request, response) -> {
            if (request.headers("Authorization") == null) {
                sparkService.halt(403, "Request denied");
            }
            response.type("application/json");
            return new GsonBuilder()
                    .create()
                    .toJson(this.dataProvider.getList());
        }));
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream stream = ApiServiceTest
                .class
                .getClassLoader()
                .getResourceAsStream("config-test.properties")) {

            properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DafApiMock mock = new DafApiMock(properties, new IntegrationTestDataProvider());
        mock.start();

        Runtime.getRuntime().addShutdownHook(new Thread(mock::stop));
    }
}
