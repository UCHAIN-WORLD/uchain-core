package com.uchain.core.datastore;

import java.util.Map;

public interface BatchSource<K, V> extends Source<K, V> {

    void updateBatch(Map<K, V> rows);
}
