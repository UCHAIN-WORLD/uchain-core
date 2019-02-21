package com.uchain;

import com.uchain.core.BlockHeader;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.LevelDBBlockChainBuilder;
import com.uchain.main.Settings;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlockChainTest {

//    static LevelDbStorage storage;
//    static LevelDBBlockChain levelDBBlockChain;
//
//    @BeforeClass
//    public static void setUpBeforeClass() throws Exception{
//        Settings settings = new Settings("config2");
//        val consensusSettings = settings.getConsensusSettings();
//        LevelDBBlockChainBuilder.populate(consensusSettings);
//        levelDBBlockChain = LevelDBBlockChainBuilder.getLevelDBBlockchain();
//        storage = levelDBBlockChain.db;
//    }
//
//    @AfterClass
//    public static void tearDownAfterClass(){
//        storage.close();
//    }

//    @Test
//    public void testInit(){
//        System.out.println(levelDBBlockChain.db);
//    }

//    @Test
//    public void testInitStorage(){
//        BlockHeader genesisBlockHeader = levelDBBlockChain.getGenesisBlockHeader();
//        val headerStore = levelDBBlockChain.getHeaderStore();
//        val value = headerStore.get(genesisBlockHeader.id());
//        assert (value == genesisBlockHeader);
//
//    }


}
