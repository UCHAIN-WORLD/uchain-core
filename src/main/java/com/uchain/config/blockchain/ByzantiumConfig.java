package com.uchain.config.blockchain;


import com.uchain.config.BlockchainConfig;
import com.uchain.config.ConstantsAdapter;
import com.uchain.core.BlockHeader;
import com.uchain.util.Constants;
import com.uchain.util.EtherUtil;

import java.math.BigInteger;


public class ByzantiumConfig extends Eip160HFConfig {

    private final Constants constants;

    public ByzantiumConfig(BlockchainConfig parent) {
        super(parent);
        constants = new ConstantsAdapter(super.getConstants()) {
            private final BigInteger BLOCK_REWARD = EtherUtil.convert(3, EtherUtil.Unit.ETHER);

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

//    @Override
//    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
//        BigInteger pd = parent.getDifficultyBI();
//        BigInteger quotient = pd.divide(getConstants().getDIFFICULTY_BOUND_DIVISOR());
//
//        BigInteger sign = getCalcDifficultyMultiplier(curBlock, parent);
//
//        BigInteger fromParent = pd.add(quotient.multiply(sign));
//        BigInteger difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), fromParent);
//
//        int explosion = getExplosion(curBlock, parent);
//
//        if (explosion >= 0) {
//            difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), difficulty.add(BigInteger.ONE.shiftLeft(explosion)));
//        }
//
//        return difficulty;
//    }

    protected int getExplosion(BlockHeader curBlock, BlockHeader parent) {
        int periodCount = (int) (Math.max(0, curBlock.getIndex() - 3_000_000) / getConstants().getEXP_DIFFICULTY_PERIOD());
        return periodCount - 2;
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        long unclesAdj = 0L;//parent.hasUncles() ? 2 : 1;
        return BigInteger.valueOf(Math.max(unclesAdj - (curBlock.getTimeStamp() - parent.getTimeStamp()) / 9, -99));
    }

    @Override
    public boolean eip198() {
        return true;
    }

    @Override
    public boolean eip206() {
        return true;
    }

    @Override
    public boolean eip211() {
        return true;
    }

    @Override
    public boolean eip212() {
        return true;
    }

    @Override
    public boolean eip213() {
        return true;
    }

    @Override
    public boolean eip214() {
        return true;
    }

    @Override
    public boolean eip658() {
        return true;
    }
}
