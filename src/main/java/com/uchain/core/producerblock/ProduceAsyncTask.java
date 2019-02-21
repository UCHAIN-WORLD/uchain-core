package com.uchain.core.producerblock;

import com.uchain.core.BlockChain;

public interface ProduceAsyncTask {
    void invoke(BlockChain chain);
}
