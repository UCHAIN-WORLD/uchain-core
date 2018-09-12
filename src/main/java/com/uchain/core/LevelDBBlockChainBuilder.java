package com.uchain.core;

import com.uchain.main.ChainSettings;
import com.uchain.main.ConsensusSettings;
import com.uchain.main.Settings;

public class LevelDBBlockChainBuilder {

    static LevelDBBlockChain chain = null;

    public static LevelDBBlockChain populate(ConsensusSettings consensusSettings){
        LevelDBBlockChain populateChain = new LevelDBBlockChain(consensusSettings);
        chain = populateChain;
        return populateChain;
    }



    public static LevelDBBlockChain getLevelDBBlockchain() {
        return chain;
    }
}
