package com.uchain.config.blockchain;

import com.uchain.core.block.BlockHeader;
import com.uchain.core.transaction.Transaction;
import com.uchain.util.Constants;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;

public class OlympicConfig extends AbstractConfig {

    public OlympicConfig() {
    }

    public OlympicConfig(Constants constants) {
        super(constants);
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return BigInteger.valueOf(curBlock.getTimeStamp() >= parent.getTimeStamp() +
                getConstants().getDURATION_LIMIT() ? -1 : 1);
    }

    @Override
    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals  = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return getGasCost().getTRANSACTION() + zeroVals * getGasCost().getTX_ZERO_DATA() +
                nonZeroes * getGasCost().getTX_NO_ZERO_DATA();
    }
}
