package com.uchain.core.producerblock;

import com.uchain.core.block.BlockChain;

public interface ProduceAsyncTask {
    void invoke(BlockChain chain);
}
