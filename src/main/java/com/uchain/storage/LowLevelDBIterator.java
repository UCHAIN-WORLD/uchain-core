package com.uchain.storage;

import com.uchain.core.consensus.TwoTuple;

public interface LowLevelDBIterator {
    void seek(byte[] prefix);

    TwoTuple<byte[],byte[]> next();

    Boolean hasNext();
}
