package com.github.italia.daf.data;

import java.io.IOException;
import java.util.List;

public interface DataProvider {
    List<EmbeddableData> getList() throws IOException;
}
