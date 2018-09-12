package com.uchain.core.consensus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.core.Block;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.Transaction;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.exceptions.UnExpectedError;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;


public class ForkBase {
	private static final Logger log = LoggerFactory.getLogger(ForkBase.class);
	
	private String dir;
	private Settings settings;
	
	private ForkItem _head;
	Map<UInt256, ForkItem> indexById = new HashMap<UInt256, ForkItem>();
	MultiMap<UInt256, UInt256> indexByPrev = new MultiMap<UInt256, UInt256>();		  
	SortedMultiMap2<Integer,Boolean,UInt256> indexByHeight = new SortedMultiMap2<Integer,Boolean,UInt256>("asc","reverse");
	SortedMultiMap2<Integer,Integer,UInt256> indexByConfirmedHeight = new SortedMultiMap2<Integer,Integer,UInt256>("reverse","reverse");
	LevelDbStorage db = ConnFacory.getInstance(dir);

	
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
	
	public ForkItem head() {
		return _head;
	}
	
	public ForkItem get(UInt256 id) {
		return indexById.get(id);
	}
	
	private void addItem(Block block,Map<PublicKey, Integer> lph) {
		BinaryData pub = block.getHeader().getProducer();
		if(lph.containsKey(PublicKey.apply(pub))) {
			lph.put(PublicKey.apply(pub), block.height());
		}
		add(new ForkItem(block, lph,false));
	}
	public Boolean add(Block block) {
		Map<PublicKey, Integer> lph = new HashMap<PublicKey, Integer>();
		if(_head == null) {
			List<Witness> witnesses = settings.getConsensusSettings().getWitnessList();
			for(Witness witness : witnesses) {
				lph.put(PublicKey.apply(new BinaryData(witness.getPubkey())), 0);
			}
			addItem(block,lph);
		}else {
			if(!indexById.containsKey(block.id()) && indexById.containsKey(block.getHeader().getPrevBlock())) {
				Map<PublicKey, Integer> lastProducerHeight= _head.getLastProducerHeight();
				lastProducerHeight.forEach((key,value) ->{
					lph.put(key, value);
				});
				addItem(block,lph);
			}else {
				return false;
			}
		}
		return null;
	}

	private Boolean add(ForkItem item) {
		if (insert(item)) {
			ForkItem oldHead = _head;
			_head = indexById.get(indexByConfirmedHeight.head().third);
			item = _head;
			removeConfirmed(item.confirmedHeight());
			if(oldHead == null || item.getBlock().prev().equals(oldHead.getBlock().id())) {
				item = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
				indexById.put(item.getBlock().id(), item);
			}else if(!item.getBlock().id().equals(oldHead.getBlock().id())) {
				switchAdd(oldHead, item);
			}
			return true;
		}else {
			return false;
		}
	}

	private void switchAdd(ForkItem from,ForkItem to) {
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = getForks(from, to);
		List<ForkItem> items = new ArrayList<ForkItem>();
		WriteBatch batch = db.getBatchWrite();
		twoTuple.first.forEach(item -> {
			ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
			batch.put(Serializabler.toBytes(newItem.getBlock().id()), newItem.toBytes());
			items.add(item);
		});
		db.BatchWrite(batch);;
		items.forEach(item -> {
			indexById.put(item.getBlock().id(), item);
		});
//		onSwitch(twoTuple.first, twoTuple.second);
	}
	
	private void removeConfirmed(int height) {
		List<ForkItem> items = new ArrayList<ForkItem>();
		SortedMultiMap2Iterator<Integer,Boolean,UInt256> iterator = indexByHeight.iterator();
		if(iterator.hasNext()) {
			ThreeTuple<Integer,Boolean,UInt256> p = iterator.next();
			if(p.first < height) {
				ForkItem item = indexById.get(p.third);
				if(item == null) {
					items.add(item);
				}
			}
		}
		WriteBatch batch = db.getBatchWrite();
		items.forEach(item -> {
			if(item.isMaster()) {
				onConfirmed(item.getBlock());
				batch.delete(Serializabler.toBytes(item.getBlock().id()));
				deleteIndex(item);
			}else {
				removeFork(item.getBlock().id());
			}
		});
		db.BatchWrite(batch);
	}
	private LevelDBBlockChain levelDBBlockChain;
	private void onConfirmed(Block block) {
		log.info("confirm block height:"+ block.height()+" block id:"+block.id());
		if(block.height() != 0) {
			levelDBBlockChain.saveBlockToStores(block);
		}
	}

//	private HeaderStore headerStore;
//	private HeightStore heightStore;
//	private HeadBlockStore headBlkStore;
//	private BlkTxMappingStore blkTxMappingStore;
//	private TransactionStore txStore;
//	private AccountStore accountStore;
//	private Boolean saveBlockToStores(Block block) {
////		WriteBatch batch = db.batchWrite();
////		headerStore.set(block.getHeader().id(), block.getHeader(), batch);
////		heightStore.set(block.getHeader().getIndex(), block.getHeader().id(), batch);
////        headBlkStore.set(HeadBlock.fromHeader(block.getHeader()), batch);
////        BlkTxMapping blkTxMapping = new BlkTxMapping(block.id(), block.getTransactionIds());
////        blkTxMappingStore.set(block.id(), blkTxMapping, batch);
////        Map<UInt160, Account> accounts = new HashMap<UInt160, Account>();
////        Map<UInt160, Map<UInt256, Fixed8>> balances = new HashMap<UInt160, Map<UInt256, Fixed8>>();
////		block.getTransactions().forEach(tx -> {
////			txStore.set(tx.id(), tx, batch);
////			calcBalancesInBlock(balances, true, tx.fromPubKeyHash(), tx.getAmount(), tx.getAssetId());
////			calcBalancesInBlock(balances, false, tx.getToPubKeyHash(), tx.getAmount(), tx.getAssetId());
////			updateAccout(accounts, tx);
////		});
////		
////		balances.forEach((k,v) -> {
////			Account account = accountStore.get(k);
////			account.getBalances();
////		});
//		
//		return false;
//	}
	
	private void calcBalancesInBlock(Map<UInt160, Map<UInt256, Fixed8>> balances,Boolean spent,
			UInt160 address,Fixed8 amounts,UInt256 assetId) {
		Fixed8 amount = null;
		if (spent) 
			amount = amounts.unary();
		else 
			amount = amounts;
		Map<UInt256, Fixed8> balance = balances.get(address);
		if(balance!=null) {
			Fixed8 value = balance.get(assetId);
			value = value.add(amount);
			balance.put(assetId, value);
		}else {
			Map<UInt256, Fixed8> map = new HashMap<UInt256, Fixed8>();
			map.put(assetId, amount);
			balances.put(address, map);
		}
	}
	
	private void updateAccout(Map<UInt160, Account> accounts, Transaction tx) {
		  // TODO
	}
	private void removeFork(UInt256 id) {
		log.info("remove fork:"+id);
		List<UInt256> list = indexByPrev.get(id);
		WriteBatch batch = db.getBatchWrite();
		for (UInt256 uInt256 : list) {
			ForkItem forkItem = indexById.get(uInt256);
			if(forkItem!=null) {
				batch.delete(Serializabler.toBytes(forkItem.getBlock().id()));
				deleteIndex(forkItem);
			}
		}
		db.BatchWrite(batch);
	}
	
	private Boolean insert(ForkItem item) {
		if (db.set(Serializabler.toBytes(item.getBlock().id()), item.toBytes())) {
			createIndex(item);
			return true;
		} else {
			return false;
		}
	}
	
	private TwoTuple<List<ForkItem>, List<ForkItem>> getForks(ForkItem x,ForkItem y) {
		ForkItem a = x;
		ForkItem b = y;
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = null;
		if(a.getBlock().id().equals(b.getBlock().id())) {
			List<ForkItem> aList = new ArrayList<ForkItem>();
			List<ForkItem> bList = new ArrayList<ForkItem>();
			aList.add(a);
			bList.add(b);
			twoTuple = new TwoTuple<List<ForkItem>, List<ForkItem>>(aList,bList);
		}else {
			List<ForkItem> xs = new ArrayList<ForkItem>();
			List<ForkItem> ys = new ArrayList<ForkItem>();
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
		indexByHeight.put(blk.height(), item.isMaster(), blk.id());
		indexByConfirmedHeight.put(item.confirmedHeight(), blk.height(), blk.id());
	}
	
	private void deleteIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.remove(blk.id());
		indexByPrev.remove(blk.prev());
		indexByHeight.remove(blk.height(), item.isMaster());
	    indexByConfirmedHeight.remove(item.confirmedHeight(), blk.height());
	}
}
