package com.uchain.config.blockchain;

import com.uchain.config.BlockchainConfig;
import com.uchain.core.transaction.Transaction;
import org.spongycastle.util.encoders.Hex;

public abstract class AbstractDaoConfig extends FrontierConfig {

    public static final long ETH_FORK_BLOCK_NUMBER = 1_920_000;
    // "dao-hard-fork" encoded message
    public static final byte[] DAO_EXTRA_DATA = Hex.decode("64616f2d686172642d666f726b");
    private final long EXTRA_DATA_AFFECTS_BLOCKS_NUMBER = 10;

    // MAYBE find a way to remove block number value from blockchain config
    protected long forkBlockNumber;

    // set in child classes
    protected boolean supportFork;

    private BlockchainConfig parent;

    protected void initDaoConfig(BlockchainConfig parent, long forkBlockNumber) {
        this.parent = parent;
        this.constants = parent.getConstants();
        this.forkBlockNumber = forkBlockNumber;
//        BlockHeaderRule rule = new ExtraDataPresenceRule(DAO_EXTRA_DATA, supportFork);
//        headerValidators().add(Pair.of(forkBlockNumber, new BlockHeaderValidator(rule)));
    }

    @Override
    public byte[] getExtraData(byte[] minerExtraData, long blockNumber) {
        if (blockNumber >= forkBlockNumber && blockNumber < forkBlockNumber + EXTRA_DATA_AFFECTS_BLOCKS_NUMBER ) {
            if (supportFork) {
                return DAO_EXTRA_DATA;
            } else {
                return new byte[0];
            }

        }
        return minerExtraData;
    }

//    @Override
//    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
//        return this.parent.calcDifficulty(curBlock, parent);
//    }

    @Override
    public long getTransactionCost(Transaction tx) {
        return parent.getTransactionCost(tx);
    }

//    @Override
//    public boolean acceptTransactionSignature(Transaction tx) {
//        return parent.acceptTransactionSignature(tx);
//    }

}
