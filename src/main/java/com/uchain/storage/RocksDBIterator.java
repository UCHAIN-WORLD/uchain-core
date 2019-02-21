package com.uchain.storage;

import com.uchain.core.consensus.TwoTuple;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.rocksdb.RocksIterator;

@Getter
@Setter
public class RocksDBIterator implements LowLevelDBIterator {

    private RocksIterator iterator;

    public RocksDBIterator(RocksIterator iterator){
        this.iterator = iterator;
    }

    @Override
    public void seek(byte[] prefix) {
        iterator.seek(prefix);
    }

    @Override
    public TwoTuple<byte[], byte[]> next() {
        TwoTuple cur = new TwoTuple<>(iterator.key(),iterator.value());
        iterator.next();
        return cur;
    }

    @Override
    public Boolean hasNext() {
        return iterator.isValid();
    }
}
