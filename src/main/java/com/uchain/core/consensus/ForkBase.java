package com.uchain.core.consensus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uchain.core.Block;
import com.uchain.core.SwitchResult;
import com.uchain.core.datastore.DataStoreConstant;
import com.uchain.core.datastore.ForkItemStore;
import com.uchain.core.datastore.SwitchStateStore;
import com.uchain.core.datastore.keyvalue.ForkItemValue;
import com.uchain.core.datastore.keyvalue.SwitchStateValue;
import com.uchain.core.datastore.keyvalue.UInt256Key;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import com.uchain.storage.Batch;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


@Setter
@Getter
public class ForkBase {
	private static final Logger log = LoggerFactory.getLogger(ForkBase.class);
	private ForkItemStore forkStore;
    private SwitchStateStore switchStateStore;
	private Settings settings;
	private ForkItem _head;
	private LevelDbStorage db;
	private FuncConfirmed funcConfirmed;
	private FuncOnSwitch funcOnSwitch;
	private Map<UInt256, ForkItem> indexById = new HashMap();
	private MultiMap<UInt256, UInt256> indexByPrev = new MultiMap();
	private SortedMultiMap2<Integer,Boolean,UInt256> indexByHeight = new SortedMultiMap2<>("asc","reverse");
	private SortedMultiMap2<Integer,Integer,UInt256> indexByConfirmedHeight = new SortedMultiMap2<>("reverse","reverse");


	public ForkBase(Settings settings, FuncConfirmed funcConfirmed,FuncOnSwitch funcOnSwitch) {
        this.settings = settings;
        this.db = ConnFacory.getInstance(settings.getChainSettings().getForkBaseSettings().getDir());
        this.forkStore = new ForkItemStore(db,settings.getChainSettings().getForkBaseSettings().getCacheSize(),DataStoreConstant.ForkItemPrefix,new UInt256Key(),new ForkItemValue());
        this.switchStateStore = new SwitchStateStore(db,DataStoreConstant.SwitchStatePrefix,new SwitchStateValue());
		init();
        this.funcConfirmed = funcConfirmed;
        this.funcOnSwitch = funcOnSwitch;
	}

    public SwitchState switchState() {
        return switchStateStore.get();
    }

    public void deleteSwitchState() {
        switchStateStore.delete(null);
    }

    public List<ForkItem> getBranch(UInt256 head, UInt256 tail) {
        ForkItem curr = indexById.get(head);
        List<ForkItem> branch = Lists.newArrayList();
        while (curr!=null && !curr.prev().equals(tail)){
            branch.add(curr);
            curr = indexById.get(curr.prev());
        }
        return Lists.reverse(branch);
    }
	/**
	 * 当前分叉头
	 * @return
	 */
	public ForkItem head() {
		return _head;
	}

    public Boolean contains(UInt256 id) {
        return indexById.containsKey(id);
    }
	/**
	 * 根据id获取ForkItem
	 * @param id
	 * @return
	 */
	public ForkItem get(UInt256 id) {
		return indexById.get(id);
	}

	public ForkItem get(int height){
        List<UInt256> list = indexByHeight.get(height,true);
        if(list == null) return null;
        if(list != null && list.size()>0) {
            UInt256 uInt256 = list.get(0);
            return get(uInt256);
        }else{
            return null;
        }
    }
	/**
	 *
	 * @param id
	 * @return
	 */
	public UInt256 getNext(UInt256 id) {
        List<UInt256> list = indexByPrev.get(id);
        if(list==null) return null;
        return list.stream().map(uInt256 -> indexById.get(uInt256)).
                filter(current -> current.isMaster()).
                findFirst().get().getBlock().id();
	}

	/**
	 * Block添加到ForkItem
	 * @param block
	 * @return
	 */
	public boolean add(Block block) {
		Map<PublicKey, Integer> lph = Maps.newHashMap();
		if(_head == null) {
			log.info("启动项目时，加入所有见证人");
			List<Witness> witnesses = settings.getConsensusSettings().getWitnessList();
			for(Witness witness : witnesses) {//map中放入所有见证人
				lph.put(PublicKey.apply(new BinaryData(witness.getPubkey())), 0);
			}
            ForkItem item = makeItem(block,lph, true);
            return add(item);//添加创世块
		}else {
			if(!indexById.containsKey(block.id())) {
                ForkItem prev = indexById.get(block.prev());
                if(prev !=null){
                    Map<PublicKey, Integer> prodHeights = prev.getLastProducerHeight();
                    Boolean master = _head.id().equals(block.prev());
                    ForkItem item = makeItem(block,prodHeights, master);
                    return add(item);//添加非创世块
                }else
                    return false;
			}else {
			    return false;
            }
		}
	}

    /**
     * 当增加一个块，对应的见证人块高度替换成最新的高度
     * @param block
     * @param heights
     * @param master
     * @return
     */
	private ForkItem makeItem(Block block,Map<PublicKey,Integer> heights,Boolean master){
	    Map<PublicKey,Integer> lph = Maps.newHashMap();
        heights.forEach((key,value) -> lph.put(key,value));
        PublicKey pub = block.getHeader().getProducer();
        if(lph.containsKey(pub)){
            lph.put(pub,block.height());
        }
        return new ForkItem(block, lph, master);
    }

    /**
     * 新添加块的上一个块和当前_head相同，进确认块
     * 新添加块和和当前_head不同，进入分叉
     * @param item
     * @return
     */
	private boolean add(ForkItem item) {
		if (insert(item)) {
            TwoTuple<ForkItem,ForkItem> twoItem = maybeReplaceHead();
            if (twoItem.second.prev().equals(twoItem.first.id())) {//当前块的上一个块和上一个块相同
                removeConfirmed(item.confirmedHeight());
            } else if (!twoItem.second.id().equals(twoItem.first.id())) {
                beginSwitch(twoItem.first, twoItem.second);
            }
            return true;
		}else {
            return false;
		}
	}

	private TwoTuple maybeReplaceHead(){
        TwoTuple<ForkItem,ForkItem> twoTuple;
        ForkItem old = resetHead();
        assert(_head != null);
        if(old == null){
            twoTuple =new TwoTuple<>(_head,_head);
        }else{
            twoTuple =new TwoTuple<>(old,_head);
        }
        return twoTuple;
    }

    private void beginSwitch(ForkItem from, ForkItem to) {
        ThreeTuple<List<ForkItem>, List<ForkItem>,SwitchState> threeTuple = getForks(from, to);
        assert (threeTuple.third != null);
        if (switchStateStore.set(threeTuple.third,null)) {
            val result = funcOnSwitch.onSwitch(threeTuple.first, threeTuple.second, threeTuple.third);
            endSwitch(threeTuple.first, threeTuple.second, result);
        } else {
            log.error("begin switch failed");
        }
    }


    public void endSwitch(List<ForkItem> oldBranch, List<ForkItem> newBranch, SwitchResult switchResult){
	    if(switchResult.getSucceed()){
            List<ForkItem> oldItems = Lists.newArrayList();
            oldBranch.forEach(forkItem -> {
                ForkItem forkItemTemp = new ForkItem(forkItem.getBlock(),forkItem.getLastProducerHeight(),false);
                oldItems.add(forkItemTemp);
            });
            List<ForkItem> newItems = Lists.newArrayList();
            newBranch.forEach(forkItem -> {
                ForkItem forkItemTemp = new ForkItem(forkItem.getBlock(),forkItem.getLastProducerHeight(),true);
                newItems.add(forkItemTemp);
            });
            Batch batch = new Batch();
            oldItems.forEach(item -> forkStore.set(item.id(), item, batch));
            newItems.forEach(item -> forkStore.set(item.id(), item, batch));
            if(db.applyBatch(batch)){
                switchStateStore.delete(batch);
                oldItems.forEach(item -> updateIndex(item));
                newItems.forEach(item -> updateIndex(item));
            }
        }else {
            deleteSwitchState();
            removeFork(switchResult.getFailedItem().id());
            resetHead();
        }
    }


    public boolean removeFork(UInt256 id){
        ForkItem item = indexById.get(id);
        if(null == item) return false;
        List<ForkItem> queue = Lists.newArrayList();
        queue.add(item);

        if (removeAll(db.batchWrite(),queue)) {
            queue.forEach(forkItem->deleteIndex(forkItem));
            return true;
        } else {
            return false;
        }
    }

    public List<ForkItem> getAncestors(List<UInt256> ancestors) {
        if(ancestors==null)
            return null;
        return ancestors.stream().map(uInt256 -> indexById.get(uInt256)).filter(forkItem -> forkItem!=null).collect(Collectors.toList());
    }

    public Boolean removeAll(Batch batch,List<ForkItem> queue){
        try {
            int i = 0;
            while (i < queue.size()) {
                ForkItem toRemove = queue.get(i);
                UInt256 toRemoveId = toRemove.id();
                List<ForkItem> ancestors = getAncestors(indexByPrev.get(toRemoveId));
                if(ancestors != null)
                    ancestors.forEach(fortItem -> queue.add(fortItem));
//                forkStore.delete(toRemoveId, batch);
                i++;
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

	public void close() {
		db.close();
	}
	/**
	 * 初始化
	 */

	public void init(){
        createIndex();
        resetHead();
	}

	private void createIndex(){
        val entryList = db.find(forkStore.getPrefixBytes());
        entryList.forEach(entry -> {
            byte[] kData = new byte[forkStore.getPrefixBytes().length];
            System.arraycopy(entry.getKey(), 0, kData, 0, forkStore.getPrefixBytes().length);
            forkStore.getKeyConverter().fromBytes(kData);
            ForkItem forkItem = forkStore.getValConverter().fromBytes(entry.getValue());
            createIndex(forkItem);
        });
    }
    private ForkItem resetHead()  {
        ForkItem old = _head;
        if(indexByConfirmedHeight.size()>0) {
            _head = indexById.get(indexByConfirmedHeight.head().third);
        }
        return old;
    }
	/**
	 * 从ForkBase删除已经已经确认的块
	 * @param height
	 * @return
	 */
	private void removeConfirmed(int height) {
        while (tryConfirm(indexByHeight.head(),height)){
            tryConfirm(indexByHeight.head(),height);
        }
	}

    private boolean tryConfirm(ThreeTuple<Integer, Boolean, UInt256> threeTuple, int height) {
        if (threeTuple.first < height) {
            ForkItem item = indexById.get(threeTuple.third);
            if (item != null) {
                if (item.isMaster()) {
                    funcConfirmed.onConfirmed(item.getBlock());
                }
                Batch batch = new Batch();
                forkStore.delete(item.id(), batch);
                if (db.applyBatch(batch)) {
                    deleteIndex(item);
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return false;
    }
	/**
	 * 存入ForkBase
	 * @param item
	 * @return
	 */
	private Boolean insert(ForkItem item) {
		if (forkStore.set(item.getBlock().id(), item,null)) {
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
	private ThreeTuple<List<ForkItem>, List<ForkItem>,SwitchState> getForks(ForkItem x,ForkItem y) {
		ForkItem a = x;
		ForkItem b = y;
        ThreeTuple<List<ForkItem>, List<ForkItem>,SwitchState> threeTuple;
		if(a.id().equals(b.id())) {
			List<ForkItem> aList = new ArrayList<>();
			List<ForkItem> bList = new ArrayList<>();
            threeTuple = new ThreeTuple(aList,bList,null);
		}else {
			List<ForkItem> xs = new ArrayList<>();
			List<ForkItem> ys = new ArrayList<>();
			while(a.height() > b.height()) {
				xs.add(a);
				a = getPrev(a);
			}
			while(b.height() > a.height()) {
				ys.add(b);
				b = getPrev(b);
			}
			while(!a.id().equals(b.id())) {
				xs.add(a);
				ys.add(b);
				a = getPrev(a);
				b = getPrev(b);
			}
			assert(a != null);
			threeTuple = new ThreeTuple(Lists.reverse(xs),Lists.reverse(ys),new SwitchState(x.id(), y.id(), a.id(), a.height()));
		}
		return threeTuple;
	}

	/**
	 * 获取上一个ForkItem
	 * @param item
	 * @return
	 */
	private ForkItem getPrev(ForkItem item) {
		ForkItem prev = get(item.getBlock().prev());
		if(prev == null) {
			throw new UnExpectedError("unexpected error");
		}
		return prev;
	}

	private void createIndex(ForkItem item) {
		Block blk = item.getBlock();
        indexById.put(blk.id(), item);
		indexByPrev.put(blk.prev(), blk.id());
		//log.info("createIndex before************************");
		//log.info(blk.height() +":::::::::::::::::" + blk.id());
		indexByHeight.put(blk.height(), item.isMaster(), blk.id());
		//log.info("createIndex after************************");
		indexByConfirmedHeight.put(item.confirmedHeight(), blk.height(), blk.id());
	}

	private void deleteIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.remove(blk.id());
		indexByPrev.remove(blk.prev());
		indexByHeight.remove(blk.height(), item.isMaster());
	    indexByConfirmedHeight.remove(item.confirmedHeight(), blk.height());
	}


	private void updateIndex(ForkItem newItem) {
		UInt256 id = newItem.id();
		int height = newItem.height();
		boolean branch = newItem.isMaster();
		List<UInt256> list = indexByHeight.remove(height, !branch);
		if(list != null) {
			for (UInt256 u : list) {
				if (!u.equals(id)) {
					indexByHeight.put(height, branch, u);
				}
			}
		}
        indexByHeight.put(height, branch, id);
		indexById.put(id, newItem);
	}

}
