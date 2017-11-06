package com.github.italia.daf.utils;

import com.github.italia.daf.data.DataProvider;
import com.github.italia.daf.data.EmbeddableData;

import java.util.ArrayList;
import java.util.List;

public class RandomDataProvider implements DataProvider {
    private List<EmbeddableData> fakeList;

    public RandomDataProvider() {

        this.fakeList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final EmbeddableData d = new EmbeddableData();
            d.setIdentifier("identifier_" + i);
            d.setOrigin(i % 2 == 0 ? "metabase" : "superset");
            d.setTitle("title_" + i);
            d.setIframeUrl("//iframe_url/" + i);
            this.fakeList.add(d);

        }
    }

    @Override
    public List<EmbeddableData> getList() {
        return this.fakeList;
    }
}
