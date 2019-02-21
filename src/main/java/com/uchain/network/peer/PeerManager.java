package com.uchain.network.peer;

import com.uchain.main.Settings;
import com.uchain.network.NetContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeerManager {

    private Settings settings;
    private NetContext netContext;

    private PeerDatabaseImpl peerDatabase = new PeerDatabaseImpl(("/peers.dat"));

    public PeerManager(Settings settings, NetContext netContext){
        if(peerDatabase.isEmpty()){
            String[] knownPeers = settings.getKnownPeers().split(",");
            for(String peer:knownPeers){

            }
        }
    }

}
