package com.uchain.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchain.common.Serializabler;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.PublicKeyHash;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Getter
@Setter
public class Transaction implements Identifier<UInt256> {

	private TransactionType txType; //交易类型
	private PublicKey from; // from
	private UInt160 toPubKeyHash; // to
	private String toName; //  发送交易者的账户
	private Fixed8 amount; //  发送金额
	private UInt256 assetId; // 资产类型
	private Long nonce; // 每发送一次交易，增加1
	private BinaryData data;// 具体数据
	private BinaryData signature;//签名
	private int version = 0x01;
	private UInt256 id = null;

	public Transaction(TransactionType txType, PublicKey from, UInt160 toPubKeyHash, String toName, Fixed8 amount,
			UInt256 assetId, Long nonce, BinaryData data, BinaryData signature, int version, UInt256 id) {
		this.txType = txType;
		this.from = from;
		this.toPubKeyHash = toPubKeyHash;
		this.toName = toName;
		this.amount = amount;
		this.assetId = assetId;
		this.nonce = nonce;
		this.data = data;
		this.signature = signature;
		this.version = 0x01;
		this.id = id;
	}

	Fixed8 fee = Fixed8.Zero;

	public UInt160 fromPubKeyHash() {// from转换和to一样格式
		return from.pubKeyHash();
	}

	public String fromAddress() { //from转换ap打头
		return from.toAddress();
	}

	public String toAddress() {  //to转换ap打头
		return PublicKeyHash.toAddress(toPubKeyHash.getData());
	}

	@Override
	public void serialize(DataOutputStream os) {
		try {
			serializeExcludeId(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Serializabler.write(os, id());
	}

	private UInt256 genId() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		try {
			serializeExcludeId(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return UInt256.fromBytes(Crypto.hash256(bs.toByteArray()));
	}

	private void serializeExcludeId(DataOutputStream os) throws IOException {
		serializeForSign(os);
		Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(signature));
	}

	// 生成签名
	public byte[] dataForSigning() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializeForSign(os);
		return bs.toByteArray();
	}
    //签名
	public void sign(PrivateKey privateKey) {
		signature = CryptoUtil
				.array2binaryData(Crypto.sign(dataForSigning(), CryptoUtil.binaryData2array(privateKey.toBin())));
	}
    //验证签名
	public boolean verifySignature() {
		return Crypto.verifySignature(dataForSigning(), CryptoUtil.binaryData2array(signature),
				CryptoUtil.binaryData2array(from.toBin()));
	}

	public static ArrayList<Transaction> transactionToArrayList(Transaction transaction){
		val list = new ArrayList<Transaction>();
		list.add(transaction);
		return list;
	}

	private void serializeForSign(DataOutputStream os) {
		try {
			os.writeByte(TransactionType.getTransactionTypeByType(txType));
			os.writeInt(version);
			Serializabler.write(os,from);
			Serializabler.write(os, toPubKeyHash);
			Serializabler.writeString(os, toName);
			Serializabler.write(os, amount);
			Serializabler.write(os, assetId);
			os.writeLong(nonce);
			Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UInt256 id() {
		if (id == null) {
			id = genId();
		}
		return id;
	}

	public static String writes(Transaction o) {
		TransactionJson transaction = new TransactionJson(o.id().toString(),
				TransactionType.getTransactionTypeStringByType(o.txType), o.fromAddress(),
				o.toAddress(), o.toName, o.amount.toString(), o.assetId.toString(), o.nonce.toString(),
				CryptoUtil.toHexString(o.data), CryptoUtil.toHexString(o.signature), String.valueOf(o.version));
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(transaction);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static Transaction deserialize(DataInputStream is) {
		try {
			val txType = TransactionType.getTransactionTypeByValue(is.readByte());
			val version = is.readInt();
			val from = PublicKey.deserialize(is);
			val toPubKeyHash = UInt160.deserialize(is);
			val toName = Serializabler.readString(is);
			val amount = Fixed8.deserialize(is);
			UInt256 assetId = UInt256.deserialize(is);
			val nonce = is.readLong();
			val data = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
			val signature = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
			return new Transaction(txType, from, toPubKeyHash, toName, amount, assetId, nonce, data, signature, version,
					UInt256.deserialize(is));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
