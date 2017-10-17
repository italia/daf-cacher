package com.github.italia.daf.utils;

import com.github.italia.daf.dafapi.HTTPClient;

import java.util.List;

public interface DataProvider {
    List<HTTPClient.EmbeddableData> getList();
}
