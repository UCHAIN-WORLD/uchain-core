package com.uchain;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: CacheTest
 *
 * @Author: bridge.bu@chinapex.com 2018/9/21 13:37
 *
 * @Version: 1.0
 * *************************************************************/

import com.uchain.common.LRUCache;
import org.junit.Test;

public class CacheTest {

    @Test
    public void testLRUCacheSetGetDelete(){
        LRUCache cache = new LRUCache<Integer,Integer>(10);
        cache.set(1, 1);
        assert(cache.get(1).equals(1));
        cache.set(1, 2);
        assert(cache.get(1).equals(2));
        cache.delete(1);
        assert(cache.get(1) == null);
    }

    @Test
    public void testLRUCacheLRU1(){
        LRUCache cache = new LRUCache<Integer,Integer>(10);
        for(int i=1;i<=11;i++){
            cache.set(i,i);
        }

        System.out.println("===============cache size is================="+cache.size());

        assert(cache.size() == 10);
        assert(cache.contains(1) == false);

        for(int i=2;i<11;i++){
            assert(cache.get(i).equals(i));
        }
    }

    @Test
    public void testLRUCacheLRU2(){
        LRUCache cache = new LRUCache<Integer,Integer>(10);
        for(int i=1;i<11;i++){
            cache.set(i,i);
        }
        //test accessOrder
        cache.get(1);
        cache.set(11, 11);
        assert(cache.get(1).equals(1));
        assert(cache.get(2) == null);
    }
}
