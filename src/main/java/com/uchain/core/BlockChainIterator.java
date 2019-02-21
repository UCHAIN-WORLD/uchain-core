package com.uchain.core;

import com.uchain.cryptohash.UInt256;
import lombok.val;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockChainIterator implements Iterator<Block> {
    private BlockChain chain;
    private UInt256 id;

    public BlockChainIterator(BlockChain chain) {
        this.chain = chain;
        this.id = chain.getLatestHeader().id();
    }

    @Override
    public boolean hasNext (){
        return !id.equals(UInt256.Zero());
    }


    public Block next(){
        if (id.equals(UInt256.Zero())) throw new NoSuchElementException();
        val blk = chain.getBlock(id);
        if(blk == null) return null;
        else {
            id = blk.getHeader().getPrevBlock();
            return blk;
        }

    }
}
