package com.uchain.core.producer;

import com.uchain.core.LevelDBBlockChain;
import com.uchain.crypto.BinaryData;
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
