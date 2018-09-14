package com.uchain.core;

import com.uchain.main.Settings;

public class LevelDBBlockChainBuilder {

    static LevelDBBlockChain chain = null;

    public static LevelDBBlockChain populate(Settings settings){
        LevelDBBlockChain populateChain = new LevelDBBlockChain(settings);
        chain = populateChain;
        return populateChain;
    }



    public static LevelDBBlockChain getLevelDBBlockchain() {
        return chain;
    }
}
