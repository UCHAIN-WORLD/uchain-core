package com.uchain.config;

import com.uchain.core.Block;
import com.uchain.core.BlockHeader;
import com.uchain.core.Transaction;
import com.uchain.core.datastore.BlockStore;
import com.uchain.util.Constants;
import com.uchain.uvm.DataWord;
import com.uchain.uvm.GasCost;
import com.uchain.uvm.OpCode;
import com.uchain.uvm.Repository;
import com.uchain.uvm.program.Program;

import java.math.BigInteger;

public interface BlockchainConfig {

    Constants getConstants();

//    MinerIfc getMineAlgorithm(SystemProperties config);

//    BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent);

    BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent);

    long getTransactionCost(Transaction tx);

//    boolean acceptTransactionSignature(Transaction tx);

    String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                      Repository repositoryTrack);


    void hardForkTransfers(Block block, Repository repo);

    byte[] getExtraData(byte[] minerExtraData, long blockNumber);

//    List<Pair<Long, BlockHeaderValidator>> headerValidators();

    GasCost getGasCost();

    DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException;

    DataWord getCreateGas(DataWord availableGas);

    boolean eip161();

    Integer getChainId();

    boolean eip198();

    boolean eip206();

    boolean eip211();

    boolean eip212();

    boolean eip213();

    boolean eip214();

    boolean eip658();

    boolean eip145();

    boolean eip1052();

    boolean eip1283();

    boolean eip1014();
}
