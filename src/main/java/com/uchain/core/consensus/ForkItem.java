package com.uchain.core.consensus;

import com.google.common.collect.Maps;
import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.cryptohash.CryptoUtil;
import com.uchain.cryptohash.PublicKey;
import com.uchain.cryptohash.UInt256;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
public class ForkItem implements Serializable {
	private Block block;
	private Map<PublicKey, Integer> lastProducerHeight; //生产者生产的最新块
	private boolean master;//是否是当前分叉

	public ForkItem(Block block, Map<PublicKey, Integer> lastProducerHeight, boolean master) {
		this.block = block;
		this.lastProducerHeight = lastProducerHeight;
		this.master = master;
	}

	private int _confirmedHeight = -1; //已确认高度

    public UInt256 id(){
        return block.id();
    }

    public UInt256 prev(){
        return  block.prev();
    }

    public int height(){return block.height();}

	/**
	 * 计算已确认高度
	 * @return
	 */
	public int confirmedHeight() {
		if (_confirmedHeight == -1) {
			int index = lastProducerHeight.size() * 2 / 3;
			List<Integer> lastHeights = lastProducerHeight.values().stream().collect(Collectors.toList());
			Collections.sort(lastHeights,Collections.reverseOrder());
			_confirmedHeight = lastHeights.get(index);
		}
		return _confirmedHeight;
	}

	/**
	 * 序列化
	 * @return
	 */
	public byte[] toBytes() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
        serialize(os);
		return bs.toByteArray();
	}

    public void serialize(DataOutputStream os) {
        block.serialize(os);
        try {
            os.writeBoolean(master);
            os.writeInt(lastProducerHeight.size());
            lastProducerHeight.forEach((key, value) -> {
                try {
                    Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(key.toBin()));
                    os.writeInt(value.intValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ForkItem deserialize(DataInputStream is){
        Block block = null;
        Boolean master = null;
        Map<PublicKey, Integer> lastProducerHeight = null;
        try {
            block = Block.deserialize(is);
            master = is.readBoolean();
            lastProducerHeight = Maps.newHashMap();
            int lastProducerHeightSize = is.readInt();
            for(int i=1;i<=lastProducerHeightSize;i++){
                lastProducerHeight.put(PublicKey.apply(CryptoUtil.array2binaryData(Serializabler.readByteArray(is))),is.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ForkItem(block, lastProducerHeight, master);

    }


	/**
	 * 反序列化
	 * @param bytes
	 * @return
	 */
	public static ForkItem fromBytes(byte[] bytes) throws IOException{
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
        return deserialize(is);
	}

}
