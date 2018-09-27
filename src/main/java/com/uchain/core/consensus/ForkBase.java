package com.uchain.core.consensus;

import com.google.common.collect.Lists;
import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.exceptions.UnExpectedError;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class ForkBase {
	private static final Logger log = LoggerFactory.getLogger(ForkBase.class);
	private Settings settings;
	private ForkItem _head;
	private LevelDbStorage db;
	private Map<UInt256, ForkItem> indexById = new HashMap<>();
	private MultiMap<UInt256, UInt256> indexByPrev = new MultiMap<UInt256, UInt256>();		  
	private SortedMultiMap2<Integer,Boolean,UInt256> indexByHeight = new SortedMultiMap2<>("asc","reverse");
	private SortedMultiMap2<Integer,Integer,UInt256> indexByConfirmedHeight = new SortedMultiMap2<>("reverse","reverse");
	
	
	public ForkBase(Settings settings) {
		String path = settings.getChainSettings().getChain_forkDir();
		this.db = ConnFacory.getInstance(path);
		this.settings = settings;
		init();
	}

	/**
	 * 当前分叉头
	 * @return
	 */
	public ForkItem head() {
		return _head;
	}
	
	/**
	 * 根据id获取ForkItem
	 * @param id
	 * @return
	 */
	public ForkItem get(UInt256 id) {
		return indexById.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public UInt256 getNext(UInt256 id) {
		UInt256 target = null;
		ForkItem current = _head;
		while(current != null) {
			if(current.getBlock().prev().equals(id)) {
				target = current.getBlock().id();
				current = null;
			}else {
				current = get(current.getBlock().prev());
			}
		}
		return target;
	}
	
	/**
	 * Block添加到ForkItem
	 * @param block
	 * @return
	 */
	public TwoTuple<List<ForkItem>,Boolean> add(Block block) {
		TwoTuple<List<ForkItem>,Boolean> twoTuple = null;
		Map<PublicKey, Integer> lph = new HashMap<PublicKey, Integer>();
		if(_head == null) {
			log.info("启动项目时，加入所有见证人");
			List<Witness> witnesses = settings.getConsensusSettings().getWitnessList();
			for(Witness witness : witnesses) {//map中放入所有见证人
				lph.put(PublicKey.apply(new BinaryData(witness.getPubkey())), 0);
			}
			twoTuple = addItem(block,lph);
		}else {
			if(!indexById.containsKey(block.id()) && indexById.containsKey(block.getHeader().getPrevBlock())) {
				Map<PublicKey, Integer> lastProducerHeight= _head.getLastProducerHeight();
				lastProducerHeight.forEach((key,value) ->{
					lph.put(key, value);
				});
				twoTuple = addItem(block,lph);
			}
		}
		return twoTuple;
	}
	
	private TwoTuple<List<ForkItem>,Boolean> addItem(Block block,Map<PublicKey, Integer> lph) {
		PublicKey pub = block.getHeader().getProducer();
		if(lph.containsKey(pub)) {
			lph.put(pub, block.height());
		}
		TwoTuple<List<ForkItem>,Boolean> twoTuple = add(new ForkItem(block, lph,false));
		return twoTuple;
	}
	
    /**
     * ForkItem
     * @param item
     * @return
     */
	private TwoTuple<List<ForkItem>, Boolean> add(ForkItem item) {
		List<ForkItem> saveBlocks = Lists.newArrayList();
		TwoTuple<List<ForkItem>,Boolean> twoTuple = null;
		if (insert(item)) {
			ForkItem oldHead = _head;
			_head = indexById.get(indexByConfirmedHeight.head().third);
			item = _head;
			saveBlocks = removeConfirmed(item.confirmedHeight());
			if(oldHead == null || item.getBlock().prev().equals(oldHead.getBlock().id())) {
				ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
				if (db.set(Serializabler.toBytes(item.getBlock().id()), newItem.toBytes())) {
					updateIndex(newItem);
				}
			}else  {
				switchAdd(oldHead, item);
			}
			twoTuple = new TwoTuple<>(saveBlocks,true);
		}else {
			twoTuple =new TwoTuple<>(saveBlocks,false);
		}
		return twoTuple;
	}
    
	/**
	 * 切换分叉
	 * @param from
	 * @param to
	 */
	private void switchAdd(ForkItem from,ForkItem to) {
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = getForks(from, to);
		List<ForkItem> items = new ArrayList<ForkItem>();
		WriteBatch batch = db.getBatchWrite();
		twoTuple.first.forEach(item -> {
			ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),false);
			batch.put(Serializabler.toBytes(newItem.getBlock().id()), newItem.toBytes());
			items.add(item);
		});
		db.BatchWrite(batch);
		items.forEach(item -> {
			ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
			batch.put(Serializabler.toBytes(newItem.getBlock().id()), newItem.toBytes());
			items.add(item);
		});
        items.forEach(item -> updateIndex(item));
	}

	public void close() {
		db.close();
	}
	/**
	 * 初始化
	 */
	private void init() {
		List<Entry<byte[], byte[]>> list = db.scan();
		for (Entry<byte[], byte[]> entry : list) {
			ForkItem item = ForkItem.fromBytes(entry.getValue());
			createIndex(item);
		}
		if (indexByConfirmedHeight.size() == 0) {
			_head = null;
		}else {
			_head = indexById.get(indexByConfirmedHeight.head().third);
		}
	}
	
	/**
	 * 从ForkBase删除已经已经确认的块
	 * @param height
	 * @return 
	 */
	private List<ForkItem> removeConfirmed(int height) {
		List<ForkItem> saveBlocks = Lists.newArrayList();
		List<ForkItem> items = new ArrayList<ForkItem>();
		ThreeTuple<Integer, Boolean, UInt256> threeTuple = indexByHeight.head();
		System.out.println("removeConfirmedremoveConfirmedremoveConfirmed");
		if(threeTuple!=null){
			System.out.println("removeConfirmedremoveConfirmedremoveConfirmed"+threeTuple.first+"   "+height);
			if(threeTuple.first < height){
				ForkItem item = indexById.get(threeTuple.third);
				System.out.println("item.isMaster()item.isMaster()item.isMaster()"+item.isMaster());
				if(item.isMaster()) {
					items.add(item);
					saveBlocks.add(item);
				}
				WriteBatch batch = db.getBatchWrite();
				batch.delete(Serializabler.toBytes(item.getBlock().id()));
				deleteIndex(item);
				db.BatchWrite(batch);
			}
		}
		System.out.println("saveBlocks="+saveBlocks.size());
		return saveBlocks;
	}

//	/**
//	 * 从当前id开始删除所以分叉
//	 * @param id
//	 */
//	private void removeFork(UInt256 id) {
//		log.info("remove fork:"+id);
//		List<UInt256> list = indexByPrev.get(id);
//		WriteBatch batch = db.getBatchWrite();
//		for (UInt256 uInt256 : list) {
//			ForkItem forkItem = indexById.get(uInt256);
//			if(forkItem!=null) {
//				batch.delete(Serializabler.toBytes(forkItem.getBlock().id()));
//				deleteIndex(forkItem);
//			}
//		}
//		db.BatchWrite(batch);
//	}
	
	/**
	 * 存入ForkBase
	 * @param item
	 * @return
	 */
	private Boolean insert(ForkItem item) {
		if (db.set(Serializabler.toBytes(item.getBlock().id()), item.toBytes())) {
			createIndex(item);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * x和y两个分叉回溯到分叉点
	 * @param x
	 * @param y
	 * @return
	 */
	private TwoTuple<List<ForkItem>, List<ForkItem>> getForks(ForkItem x,ForkItem y) {
		ForkItem a = x;
		ForkItem b = y;
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = null;
		if(a.getBlock().id().equals(b.getBlock().id())) {
			List<ForkItem> aList = new ArrayList<>();
			List<ForkItem> bList = new ArrayList<>();
			aList.add(a);
			bList.add(b);
			twoTuple = new TwoTuple<>(aList,bList);
		}else {
			List<ForkItem> xs = new ArrayList<>();
			List<ForkItem> ys = new ArrayList<>();
			while(a.getBlock().getHeader().getIndex() < b.getBlock().getHeader().getIndex()) {
				xs.add(a);
				a = getPrev(a);
			}
			while(b.getBlock().getHeader().getIndex() < a.getBlock().getHeader().getIndex()) {
				ys.add(b);
				b = getPrev(b);
			}
			while(!a.getBlock().id().equals(b.getBlock().id())) {
				xs.add(a);
				ys.add(b);
				a = getPrev(a);
				b = getPrev(b);
			}
			twoTuple = new TwoTuple<List<ForkItem>, List<ForkItem>>(xs,ys);
		}
		return twoTuple;
	}
	
	/**
	 * 获取上一个ForkItem
	 * @param item
	 * @return
	 */
	private ForkItem getPrev(ForkItem item) {
		ForkItem prev = get(item.getBlock().getHeader().getPrevBlock());
		if(prev == null) {
			throw new UnExpectedError("unexpected error");
		}
		return prev;
	}
	
	private void createIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.put(blk.id(), item);
		indexByPrev.put(blk.prev(), blk.id());
		System.out.println("存入的高度="+item.confirmedHeight()+"  "+blk.height()+"  "+blk.id().toString());
		indexByHeight.put(blk.height(), item.isMaster(), blk.id());
		indexByConfirmedHeight.put(item.confirmedHeight(), blk.height(), blk.id());
	}
	
	private void deleteIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.remove(blk.id());
		indexByPrev.remove(blk.prev());
		System.out.println("删除的高度="+item.confirmedHeight()+"  "+blk.height()+"  "+blk.id().toString());
		indexByHeight.remove(blk.height(), item.isMaster());
	    indexByConfirmedHeight.remove(item.confirmedHeight(), blk.height());
	}


	private void updateIndex(ForkItem newItem) {
		UInt256 id = newItem.getBlock().id();
		int height = newItem.getBlock().height();
		boolean branch = newItem.isMaster();
		List<UInt256> list = indexByHeight.remove(height, !branch);
		if(list != null) {
			for (UInt256 u : list) {
				if (!u.equals(id)) {
					System.out.println("删除的高度1="+height);
					indexByHeight.put(height, branch, u);
				}
			}
		}
		System.out.println("删除的高度2="+height);
        indexByHeight.put(height, branch, id);
		indexById.put(id, newItem);
	}
}
