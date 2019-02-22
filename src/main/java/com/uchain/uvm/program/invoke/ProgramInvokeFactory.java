package com.uchain.uvm.program.invoke;

import com.uchain.core.block.Block;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.datastore.BlockStore;
import com.uchain.cryptohash.UInt160;
import com.uchain.uvm.DataWord;
import com.uchain.uvm.Repository;
import com.uchain.uvm.program.Program;

import java.math.BigInteger;

public interface ProgramInvokeFactory {

/*    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, Repository origRepository, BlockStore blockStore);*/
    ProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                      Repository repository, Repository origRepository, UInt160 newContractAddress, BlockStore blockStore);
    ProgramInvoke createProgramInvoke(Program program, DataWord toAddress, DataWord callerAddress,
                                      DataWord inValue, DataWord inGas,
                                      BigInteger balanceInt, byte[] dataIn,
                                      Repository repository, Repository origRepository, BlockStore blockStore,
                                      boolean staticCall, boolean byTestingSuite);
}
