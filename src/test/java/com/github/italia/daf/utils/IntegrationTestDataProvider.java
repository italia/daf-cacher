package com.github.italia.daf.utils;

import com.github.italia.daf.dafapi.HTTPClient;
import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationTestDataProvider implements DataProvider {

    private List<HTTPClient.EmbeddableData> list;

    public IntegrationTestDataProvider() {
        this.list = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            String result = IOUtils.toString(classLoader.getResourceAsStream("integration-data.json"), "UTF-8");
            this.list = new GsonBuilder().create().fromJson(result, new TypeToken<List<HTTPClient.EmbeddableData>>() {
            }.getType());
        } catch (IOException e) {
            /* ignored */
        }
    }

    @Override
    public List<HTTPClient.EmbeddableData> getList() {
        return list;
    }
}
