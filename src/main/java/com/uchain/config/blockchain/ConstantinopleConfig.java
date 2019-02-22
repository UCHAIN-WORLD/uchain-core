package com.uchain.config.blockchain;


import com.uchain.config.BlockchainConfig;
import com.uchain.config.ConstantsAdapter;
import com.uchain.core.block.BlockHeader;
import com.uchain.util.Constants;
import com.uchain.util.MonetaryUtil;

import java.math.BigInteger;

public class ConstantinopleConfig extends ByzantiumConfig {

    private final Constants constants;

    public ConstantinopleConfig(BlockchainConfig parent) {
        super(parent);
        constants = new ConstantsAdapter(super.getConstants()) {
            private final BigInteger BLOCK_REWARD = MonetaryUtil.convert(2, MonetaryUtil.Unit.ETHER);

            @Override
            public BigInteger getBLOCK_REWARD() {
                return BLOCK_REWARD;
            }
        };
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    protected int getExplosion(BlockHeader curBlock, BlockHeader parent) {
        int periodCount = (int) (Math.max(0, curBlock.getIndex() - 5_000_000) / getConstants().getEXP_DIFFICULTY_PERIOD());
        return periodCount - 2;
    }

    @Override
    public boolean eip1052() {
        return true;
    }

    @Override
    public boolean eip145() {
        return true;
    }

    @Override
    public boolean eip1283() {
        return true;
    }

    @Override
    public boolean eip1014() {
        return true;
    }
}
