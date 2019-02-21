package com.uchain.vm.program.invoke;

import com.uchain.core.Block;
import com.uchain.core.Transaction;
import com.uchain.core.datastore.AccountStore;
import com.uchain.core.datastore.BlockStore;
import com.uchain.crypto.UInt160;
import com.uchain.vm.DataWord;
import com.uchain.vm.Repository;
import com.uchain.vm.program.Program;

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
