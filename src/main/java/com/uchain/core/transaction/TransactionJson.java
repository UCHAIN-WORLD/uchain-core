package com.uchain.core.transaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionJson {
	private String id;
	private String type;
	private String from;
	private String to;
	private String toName;
	private String amount;
	private String assetId;
	private String nonce;
	private String data;
	private String signature;
	private String version;
	
	public TransactionJson(String id, String type, String from, String to, String toName, String amount, String assetId,
			String nonce, String data, String signature, String version) {
		super();
		this.id = id;
		this.type = type;
		this.from = from;
		this.to = to;
		this.toName = toName;
		this.amount = amount;
		this.assetId = assetId;
		this.nonce = nonce;
		this.data = data;
		this.signature = signature;
		this.version = version;
	}
	
	
}
