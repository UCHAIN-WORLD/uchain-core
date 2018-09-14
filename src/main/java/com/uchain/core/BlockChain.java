package com.uchain.core;

import java.util.List;
import java.util.Map;

import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

public interface BlockChain extends Iterable<Block>{

    BlockHeader getLatestHeader();

    int getHeight();

    long getHeadTime();

    long headTimeSinceGenesis();

    long getDistance();

    BlockHeader getHeader(UInt256 id);

    BlockHeader getHeader(int index);
    
    UInt256 getNextBlockId(UInt256 id);

    Block getBlock(int height);

    Block getBlock(UInt256 id);
    
    Block getBlockInForkBase(UInt256 id);

    boolean containsBlock(UInt256 id);

    boolean tryInsertBlock(Block block);

    Transaction getTransaction(UInt256 id);

    boolean containsTransaction(UInt256 id);

    boolean verifyBlock(Block block);

    boolean verifyTransaction(Transaction tx);

    Map<UInt256, Long> getBalance(UInt160 address);

    String getGenesisBlockChainId();

    Block produceBlock(PublicKey producer, PrivateKey privateKey, long timeStamp,
                              List<Transaction> txs);
    Account getAccount(UInt160 address);
}