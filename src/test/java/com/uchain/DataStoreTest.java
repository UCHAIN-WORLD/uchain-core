package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: DataStoreTest
 *
 * @Author: bridge.bu@chinapex.com 2018/9/26 10:07
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.core.datastore.HeaderStore;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.val;
import org.junit.Test;

public class DataStoreTest {

    private static LevelDbStorage storage;
    /*
    @Test
    public void testCommitRollBack(){
        val db = LevelDbStorage.open("test_RollBack");
        storage = ConnFacory.getInstance("\\.\\test_RollBack");
        val store = new HeaderStore(storage, 10);
        val blk1 = createBlockHeader;
        System.out.println(blk1.id)
        store.beginTransaction()
        store.set(blk1.id, blk1)
        assert(store.get(blk1.id).get.equals(blk1))
        store.rollBack()
        assert(store.get(blk1.id).isEmpty)
        val blk2 = createBlockHeader
        println(blk2.id)
        store.set(blk2.id, blk2)
        store.commit()
        assert(store.get(blk2.id).get.equals(blk2))
        store.beginTransaction()
        val blk3 = createBlockHeader
        println(blk3.id)
        store.set(blk3.id, blk3)
        assert(store.get(blk3.id).get.equals(blk3))
        store.rollBack()
        assert(store.get(blk2.id).get.equals(blk2))
        assert(store.get(blk3.id).isEmpty)
        db.close()
        Directory("test_RollBack").deleteRecursively()
    }
*/
}
