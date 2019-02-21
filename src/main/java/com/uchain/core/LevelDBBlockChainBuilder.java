package com.uchain.core;

import com.uchain.main.Settings;
import com.uchain.networkmanager.Node;

public class LevelDBBlockChainBuilder {

    public static LevelDBBlockChain populate(Settings settings, NotificationOnBlock notificationOnBlock, NotificationOnTransaction notificationOnTransaction, Node nodeActor) {
        LevelDBBlockChain populateChain = new LevelDBBlockChain(settings, notificationOnBlock, notificationOnTransaction, nodeActor);
        return populateChain;
    }
}
