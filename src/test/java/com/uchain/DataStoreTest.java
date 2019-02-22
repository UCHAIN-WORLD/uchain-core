package com.uchain;

import com.uchain.core.block.BlockHeader;
import com.uchain.core.datastore.DataStoreConstant;
import com.uchain.core.datastore.HeaderStore;
import com.uchain.core.datastore.keyvalue.BlockHeaderValue;
import com.uchain.core.datastore.keyvalue.UInt256Key;
import com.uchain.cryptohash.BinaryData;
import com.uchain.cryptohash.PrivateKey;
import com.uchain.cryptohash.PublicKey;
import com.uchain.cryptohash.UInt256;
import com.uchain.storage.LevelDbStorage;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DataStoreTest {

    private static final List<String> dirs = new ArrayList<String>();
    private static final List<LevelDbStorage>  dbs = new ArrayList<LevelDbStorage>();

    public static LevelDbStorage openDB(String dir){
        LevelDbStorage db = LevelDbStorage.open(dir);
        if (!dirs.contains(dir)) {
            dirs.add(dir);
        }
        dbs.add(db);
        return db;
    }

    public static void closeDB(LevelDbStorage db){
        db.close();
        dbs.remove(db);
    }

    @AfterClass
    public static void cleanUp(){
        dbs.forEach(dbtmp->{dbtmp.close();});
        dirs.forEach(dirtmp->deleteDir(dirtmp));
    }

    private static void deleteDir(String dir){
        try {
            File scFileDir = new File(dir);
            File TrxFiles[] = scFileDir.listFiles();
            for(File curFile:TrxFiles ){
                curFile.delete();
            }
            scFileDir.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static BlockHeader createBlockHeader(){
        try {
            UInt256 prevBlock = SerializerTest.testHash256("prev");
            UInt256 merkleRoot = SerializerTest.testHash256("root");
            PublicKey producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682")); // TODO: read from settings
            PrivateKey producerPrivKey = PrivateKey.apply(new BinaryData("7a93d447bffe6d89e690f529a3a0bdff8ff6169172458e04849ef1d4eafd7f86"));
            Long timeStamp = Instant.now().toEpochMilli();
            return new BlockHeader(0, timeStamp, merkleRoot, prevBlock, producer, new BinaryData("0000"),0x01,null);//null 代替Uint256
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    @Test
    public void testCommitRollBack(){
        try{
            LevelDbStorage db = DataStoreTest.openDB("test_rollBack");
            HeaderStore store = new HeaderStore(db, 10, DataStoreConstant.HeaderPrefix,
                new UInt256Key(), new BlockHeaderValue());
            BlockHeader blk1 = createBlockHeader();
            assert (blk1 != null);
            db.newSession();
            store.set(blk1.id(), blk1,null);
            assert(store.get(blk1.id()).equals(blk1));
            db.rollBack();
            assert(store.get(blk1.id()) == null);
            BlockHeader blk2 = createBlockHeader();
//            db.newSession();
            store.set(blk2.id(), blk2,null);
            db.commit();
            assert(store.get(blk2.id()).equals(blk2));
            db.rollBack();
            assert(store.get(blk2.id()).equals(blk2));
            db.newSession();
            BlockHeader blk3 = createBlockHeader();
            store.set(blk3.id(), blk3,null);
            assert(store.get(blk3.id()).equals(blk3));
            db.rollBack();
            assert(store.get(blk2.id()).equals(blk2));
            assert(store.get(blk3.id()) == null);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
