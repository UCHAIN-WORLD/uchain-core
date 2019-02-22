package com.uchain.config.blockchain;

import com.uchain.config.BlockchainConfig;
import com.uchain.config.BlockchainNetConfig;
import com.uchain.core.block.Block;
import com.uchain.core.block.BlockHeader;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.datastore.BlockStore;
import com.uchain.util.Constants;
import com.uchain.uvm.DataWord;
import com.uchain.uvm.GasCost;
import com.uchain.uvm.OpCode;
import com.uchain.uvm.Repository;
import com.uchain.uvm.program.Program;


public abstract class AbstractConfig implements BlockchainConfig, BlockchainNetConfig {
    private static final GasCost GAS_COST = GasCost.getInstance();

    protected Constants constants;
//    protected MinerIfc miner;
//    private List<Pair<Long, BlockHeaderValidator>> headerValidators = new ArrayList<>();

    public AbstractConfig() {
        this(new Constants());
    }

    public AbstractConfig(Constants constants) {
        this.constants = constants;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockHeader) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return getConstants();
    }

//    @Override
//    public MinerIfc getMineAlgorithm(SystemProperties config) {
//        if (miner == null) miner = new EthashMiner(config);
//        return miner;
//    }

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
        int periodCount = (int) (curBlock.getIndex() / getConstants().getEXP_DIFFICULTY_PERIOD());
        return periodCount - 2;
    }

//    @Override
//    public boolean acceptTransactionSignature(Transaction tx) {
//        return Objects.equals(tx.getChainId(), getChainId());
//    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                             Repository repository) {
        return null;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {}

    @Override
    public byte[] getExtraData(byte[] minerExtraData, long blockNumber) {
        return minerExtraData;
    }

//    @Override
//    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
//        return headerValidators;
//    }


    @Override
    public GasCost getGasCost() {
        return GAS_COST;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        if (requestedGas.compareTo(availableGas) > 0) {
            throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        }
        return requestedGas;
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return availableGas;
    }

    @Override
    public boolean eip161() {
        return false;
    }

    @Override
    public Integer getChainId() {
        return null;
    }

    @Override
    public boolean eip198() {
        return false;
    }

    @Override
    public boolean eip206() {
        return false;
    }

    @Override
    public boolean eip211() {
        return false;
    }

    @Override
    public boolean eip212() {
        return false;
    }

    @Override
    public boolean eip213() {
        return false;
    }

    @Override
    public boolean eip214() {
        return false;
    }

    @Override
    public boolean eip658() {
        return false;
    }

    @Override
    public boolean eip1052() {
        return false;
    }

    @Override
    public boolean eip145() {
        return false;
    }

    @Override
    public boolean eip1283() {
        return false;
    }

    @Override
    public boolean eip1014() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
