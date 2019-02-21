package com.uchain.core.producerblock;

import com.uchain.core.LevelDBBlockChain;
import com.uchain.cryptohash.BinaryData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendRawTransaction {
    private BinaryData rawTx;
    private LevelDBBlockChain chain;
    public SendRawTransaction(BinaryData rawTx, LevelDBBlockChain chain){
        this.rawTx = rawTx;
        this.chain = chain;
    }
}
