package com.uchain.core;

import com.uchain.main.ChainSettings;
import com.uchain.main.ConsensusSettings;

public class LevelDBBlockChainBuilder {

    static LevelDBBlockChain chain = null;

    static LevelDBBlockChain populate(ChainSettings chainSettings, ConsensusSettings consensusSettings){
        LevelDBBlockChain populateChain = new LevelDBBlockChain(chainSettings, consensusSettings);
        chain = populateChain;
        return populateChain;
    }

    static LevelDBBlockChain getLevelDBBlockchain() {
        return chain;
    }
}
