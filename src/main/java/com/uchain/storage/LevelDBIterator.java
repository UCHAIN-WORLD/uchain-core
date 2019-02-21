package com.uchain.storage;

import com.uchain.core.consensus.TwoTuple;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.iq80.leveldb.DBIterator;

@Getter
@Setter
public class LevelDBIterator implements LowLevelDBIterator {

    private DBIterator iterator;

    public LevelDBIterator(DBIterator iterator){
        this.iterator = iterator;
    }

    @Override
    public void seek(byte[] prefix) {
        iterator.seek(prefix);
    }

    @Override
    public TwoTuple<byte[], byte[]> next() {
        val entry = iterator.next();
        return new TwoTuple<>(entry.getKey(),entry.getValue());
    }

    @Override
    public Boolean hasNext() {
        return iterator.hasNext();
    }
}
