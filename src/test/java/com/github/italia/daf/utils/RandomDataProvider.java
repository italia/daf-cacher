package com.github.italia.daf.utils;

import com.github.italia.daf.dafapi.HTTPClient;

import java.util.ArrayList;
import java.util.List;

public class RandomDataProvider implements DataProvider {
    private List<HTTPClient.EmbeddableData> fakeList;

    public RandomDataProvider() {

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

    @Override
    public List<HTTPClient.EmbeddableData> getList() {
        return this.fakeList;
    }
}
