package com.uchain.core;

import com.uchain.crypto.UInt256;
import com.uchain.crypto.*;
import lombok.val;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockChainIterator implements Iterator<Block> {
    BlockChain chain;
    UInt256 id = chain.getLatestHeader().id();

    @Override
    public boolean hasNext (){
        return !id.equals(UInt256Util.Zero());
    }


    public Block next(){
        if (id.equals(UInt256Util.Zero())) throw new NoSuchElementException();
        val blk = chain.getBlock(id);
        if(blk == null) return blk;
        else {
            id = blk.getHeader().getPrevBlock();
            return blk;
        }

    }
}
