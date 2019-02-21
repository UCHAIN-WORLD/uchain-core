package com.uchain.config.blockchain;


import com.uchain.config.BlockchainConfig;
import com.uchain.config.BlockchainNetConfig;
import com.uchain.util.Constants;

public class BaseNetConfig implements BlockchainNetConfig {
    private long[] blockNumbers = new long[64];
    private BlockchainConfig[] configs = new BlockchainConfig[64];
    private int count;

    public void add(long startBlockNumber, BlockchainConfig config) {
        if (count >= blockNumbers.length) throw new RuntimeException();
        if (count > 0 && blockNumbers[count] >= startBlockNumber)
            throw new RuntimeException("Block numbers should increase");
        if (count == 0 && startBlockNumber > 0) throw new RuntimeException("First config should start from block 0");
        blockNumbers[count] = startBlockNumber;
        configs[count] = config;
        count++;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockNumber) {
        for (int i = 0; i < count; i++) {
            if (blockNumber < blockNumbers[i]) return configs[i - 1];
        }
        return configs[count - 1];
    }

    @Override
    public Constants getCommonConstants() {
        // TODO make a guard wrapper which throws exception if the requested constant differs among configs
        return configs[0].getConstants();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("BaseNetConfig{")
                .append("blockNumbers= ");

        for (int i = 0; i < count; ++i) {
            res.append("#").append(blockNumbers[i]).append(" => ");
            res.append(configs[i]);
            if (i != count - 1) {
                res.append(", ");
            }
        }

        res.append(" (total: ").append(count).append(")}");

        return res.toString();
    }
}
