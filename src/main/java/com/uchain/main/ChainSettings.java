package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChainSettings {
    private String chain_dbDir;
    private String chain_forkDir;
    private String chain_miner;
    private Long chain_genesis_timeStamp;
    private String chain_genesis_publicKey;
    private String chain_genesis_privateKey;
    
	public ChainSettings(String chain_dbDir, String chain_forkDir, String chain_miner, Long chain_genesis_timeStamp,
			String chain_genesis_publicKey, String chain_genesis_privateKey) {
		this.chain_dbDir = chain_dbDir;
		this.chain_forkDir = chain_forkDir;
		this.chain_miner = chain_miner;
		this.chain_genesis_timeStamp = chain_genesis_timeStamp;
		this.chain_genesis_publicKey = chain_genesis_publicKey;
		this.chain_genesis_privateKey = chain_genesis_privateKey;
	}

    
}
