package com.uchain.core;

import com.uchain.core.datastore.*;
import com.uchain.core.datastore.keyvalue.*;
import com.uchain.crypto.*;
import com.uchain.main.ChainSettings;
import com.uchain.main.ConsensusSettings;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.iq80.leveldb.WriteBatch;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface BlockChain extends Iterable<Block>{

    BlockHeader getLatestHeader();

    int getHeight();

    long getHeadTime();

    long headTimeSinceGenesis();

    long getDistance();

    BlockHeader getHeader(UInt256 id);

    BlockHeader getHeader(int index);

    Block getBlock(int height);

    Block getBlock(UInt256 id);

    boolean containsBlock(UInt256 id);

    boolean tryInsertBlock(Block block);

    Transaction getTransaction(UInt256 id);

    boolean containsTransaction(UInt256 id);

    boolean verifyBlock(Block block);

    boolean verifyTransaction(Transaction tx);

    Map<UInt256, Long> getBalance(UInt160 address);

    String getGenesisBlockChainId();

    Block produceBlock(PublicKey producer, PrivateKey privateKey, long timeStamp,
                              ArrayList<Transaction> transactions);

}