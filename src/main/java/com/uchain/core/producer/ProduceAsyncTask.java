package com.uchain.core.producer;

import com.uchain.core.BlockChain;

public interface ProduceAsyncTask {
    void invoke(BlockChain chain);
}
