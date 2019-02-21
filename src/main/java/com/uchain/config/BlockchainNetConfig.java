package com.uchain.config;


import com.uchain.util.Constants;

public interface BlockchainNetConfig {

    BlockchainConfig getConfigForBlock(long blockNumber);

    Constants getCommonConstants();
}
