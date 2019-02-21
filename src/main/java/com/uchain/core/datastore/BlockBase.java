package com.uchain.core.datastore;

import com.uchain.core.Block;
import com.uchain.core.BlockHeader;
import com.uchain.core.datastore.keyvalue.*;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.BlockBaseSettings;
import com.uchain.storage.Batch;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockBase{

    LevelDbStorage db;
    BlockBaseSettings settings;

    private BlockStore blockStore;

    private HeadBlockStore headBlkStore;

    private HeightStore heightStore;

    public BlockBase(BlockBaseSettings settings){
        this.settings = settings;
        this.db = ConnFacory.getInstance(settings.getDir());
        this.blockStore = new BlockStore(db, settings.getCacheSize(),DataStoreConstant.BlockPrefix, new UInt256Key(),
                new BlockValue());
        this.headBlkStore = new HeadBlockStore(db, DataStoreConstant.HeadBlockStatePrefix,
                new BlockHeaderValue());

        this.heightStore = new HeightStore(db, settings.getCacheSize(), DataStoreConstant.HeightToIdIndexPrefix,
                new IntKey(), new UInt256Value());
    }

    public  void add (Block block){
        if (head()== null || head().id().toString().equals(block.prev().toString())){
            Batch batch = db.batchWrite();
            blockStore.set(block.id(), block, batch);
            heightStore.set(block.height(), block.id(), batch);
            headBlkStore.set(block.getHeader(), batch);
            db.applyBatch(batch);
        }
        else throw new IllegalArgumentException("requirement failed");
    }

    public BlockHeader head(){
        return headBlkStore.get();
    }

    public Block getBlock(UInt256 id){
        return blockStore.get(id);
    }

    public Block getBlock(int height){
        UInt256 key = heightStore.get(height);
        if(key!=null) {
            return getBlock(key);
        }else {
            return null;
        }
    }

    public Boolean containBlock(UInt256 id) {
        return blockStore.contains(id);
    }

    public void close(){
        db.close();
    }
}
