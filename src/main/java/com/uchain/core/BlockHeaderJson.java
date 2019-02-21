package com.uchain.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockHeaderJson {
	private String id;
	private int index;
	private Long timeStamp;
	private String merkleRoot;
	private String prevBlock;
	private String producer;
	private String producerSig;
	private int version;
	
	public BlockHeaderJson() {}
	public BlockHeaderJson(String id, int index, Long timeStamp, String merkleRoot, String prevBlock, String producer,
			String producerSig, int version) {
		this.id = id;
		this.index = index;
		this.timeStamp = timeStamp;
		this.merkleRoot = merkleRoot;
		this.prevBlock = prevBlock;
		this.producer = producer;
		this.producerSig = producerSig;
		this.version = version;
	}
	
}
