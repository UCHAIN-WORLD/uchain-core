package com.uchain.core.consensus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.PublicKey;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Setter
@Getter
public class ForkItem {
	private Block block;
	private Map<PublicKey, Integer> lastProducerHeight; //生产者生产的最新块
	private boolean master;//是否是当前分叉

	public ForkItem(Block block, Map<PublicKey, Integer> lastProducerHeight, boolean master) {
		this.block = block;
		this.lastProducerHeight = lastProducerHeight;
		this.master = false;
	}

	private int _confirmedHeight = -1; //已确认高度

	/**
	 * 计算已确认高度
	 * @return
	 */
	public int confirmedHeight() {
		if (_confirmedHeight == -1) {
			int index = lastProducerHeight.size() * 2 / 3;
			List<Integer> lastHeights = new ArrayList<Integer>();
			for (Integer value : lastProducerHeight.values()) {
				lastHeights.add(value);
			}
			Collections.sort(lastHeights, new Comparator<Integer>() {
				public int compare(Integer o1, Integer o2) {
					// 按照金额大小进行降序排列
					if (o1.intValue() < o2.intValue()) {
						return 1;
					}
					if (o1.intValue() == o2.intValue()) {
						return 0;
					}
					return -1;
				}
			});
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
		return bs.toByteArray();
	}

	/**
	 * 反序列化
	 * @param bytes
	 * @return
	 */
	public static ForkItem fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			val block = Block.deserialize(is);
			val master = is.readBoolean();
			val lastProducerHeight = new HashMap<PublicKey, Integer>();
			int lastProducerHeightSize = is.readInt();
			for(int i=1;i<=lastProducerHeightSize;i++) {
			 lastProducerHeight.put(PublicKey.apply(CryptoUtil.array2binaryData(Serializabler.readByteArray(is))), is.readInt());
			}
			return new ForkItem(block, lastProducerHeight, master);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
