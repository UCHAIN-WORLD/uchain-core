package com.uchain.core.block;

import com.uchain.core.Account;
import com.uchain.core.ChainInfo;
import com.uchain.core.transaction.Transaction;
import com.uchain.core.transaction.TransactionReceipt;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.Witness;
import com.uchain.uvm.Repository;

import java.math.BigInteger;
import java.util.Map;

public interface BlockChain extends Iterable<Block>{

    ChainInfo getChainInfo();

    BlockHeader getLatestHeader();

    int getHeight();

    long getHeadTime();

    long headTimeSinceGenesis();

    BlockHeader getHeader(UInt256 id);

    BlockHeader getHeader(int index);
    
    UInt256 getNextBlockId(UInt256 id);

    Block getBlock(int height);

    Block getBlock(UInt256 id);

    Boolean containsBlock(UInt256 id);

    Transaction getPendingTransaction(UInt256 txid);
    
    void startProduceBlock(Witness producer, long blockTime);

    boolean produceBlockAddTransaction(Transaction tx);

    Block produceBlockFinalize(long endTime);

    Boolean isProducingBlock();

    Boolean addTransaction(Transaction tx);

    Boolean addTransactionReceipt(TransactionReceipt transactionReceipt);

    TransactionReceipt getTransactionReceipt(UInt256 id);

    TransactionReceipt executeTransaction(Transaction tx,long stopProcessTxTime);

    Boolean tryInsertBlock(Block block,Boolean doApply);

    Transaction getTransaction(UInt256 id);

    boolean containsTransaction(UInt256 id);

//    boolean verifyBlock(Block block);
//
//    boolean verifyTransaction(Transaction tx);

    Map<UInt256, BigInteger> getBalance(UInt160 address);

    Account getAccount(UInt160 address);

    String Id();

    void close();

    Repository getRepository();
}