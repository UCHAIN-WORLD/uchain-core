package com.uchain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
import com.uchain.util.*;
import com.uchain.vm.DataWord;
import com.uchain.vm.GasCost;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.api.libs.json.Writes;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.uchain.util.BIUtil.toBI;
import static com.uchain.util.ByteUtil.longToBytes;

@Getter
@Setter
public class Transaction implements Identifier<UInt256> {
	private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
	private TransactionType txType; //交易类型

	@JsonIgnore
	protected UInt160 from; // from
	@JsonIgnore
	private UInt160 toPubKeyHash; // to
	private String toName; //  发送交易者的账户
    @JsonIgnore
	private Fixed8 amount; //  发送金额
    @JsonIgnore
	private UInt256 assetId; // 资产类型
	private Long nonce; // 每发送一次交易，增加1
    @JsonIgnore
	private BinaryData data;// 具体数据
    @JsonIgnore
	private BinaryData signature;//签名
	private int version = 0x01;
    @JsonIgnore
	private UInt256 id = null;
    @JsonIgnore
    private byte[] gasPrice;
    @JsonIgnore
    private byte[] gasLimit;

    public UInt256 getId() {
        if (id == null) {
            id = genId();
        }
        return id;
    }


	public Transaction(TransactionType txType, UInt160 from, UInt160 toPubKeyHash, String toName, Fixed8 amount,
			UInt256 assetId, Long nonce, BinaryData data, BinaryData signature, int version, UInt256 id) {
		this(txType,from,toPubKeyHash,toName,amount,assetId,nonce,data,signature,version,id
				,ByteUtil.hexStringToBytes(GasCost.getInstance().getGasPrice())
				,ByteUtil.hexStringToBytes(GasCost.getInstance().getGasLimit()));
	}

	public Transaction(byte[] rawData){
		val bis = new ByteArrayInputStream(rawData);
		val is = new DataInputStream(bis);
		Transaction tmp = deserialize(is);
        this.txType = tmp.txType;
        this.from = tmp.from;
        this.toPubKeyHash = tmp.toPubKeyHash;
        this.toName = tmp.toName;
        this.amount = tmp.amount;
        this.assetId = tmp.assetId;
        this.nonce = tmp.nonce;
        this.data = tmp.data;
        this.signature = tmp.signature;
        this.version = tmp.version;
        this.id = tmp.id;
        this.gasPrice = tmp.gasPrice;
        this.gasLimit = tmp.gasLimit;
	}

    @JsonIgnore
	Fixed8 fee = Fixed8.Zero;

	public UInt160 fromPubKeyHash() {
		return from;
	}

	public String fromAddress() {
		return from.toAddressString();
	}

	public String toAddress() {
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
				.array2binaryData(Crypto.sign(dataForSigning(), privateKey));
	}
    //验证签名
	public boolean verifySignature() {
		if(txType == TransactionType.Estimate_ContractConsumer) return true;
		val message = dataForSigning();
		if (Crypto.verifySignature(message, signature)) {
			List<PublicKey> pubs = Crypto.recoverPublicKey(signature, message);
			val from1 = pubs.get(0).pubKeyHash();
			val from2 = pubs.get(1).pubKeyHash();
			if (from1.equals(from)  || from2.equals(from) )
				return true;
			else
				return false;
		}
		else
			return false;
	}

	public static ArrayList<Transaction> transactionToArrayList(Transaction transaction){
		val list = new ArrayList();
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
            Serializabler.writeByteArray(os, gasPrice);
            Serializabler.writeByteArray(os, gasLimit);
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

	@JsonProperty("id")
    public String reJsonId(){
	    if(id == null) return "";
	    return id.toString();
    }

    @JsonProperty
    public String from(){
        if(from == null) return "";
        return from.toAddressString();
    }

    @JsonProperty
    public String to(){
        if(toPubKeyHash == null) return "";
        return toAddress();
    }

    @JsonProperty
    public String amount(){
        if(amount == null) return "";
        return amount.toString();
    }

    @JsonProperty
    public String assetId(){
        if(assetId == null) return "";
        return assetId.toString();
    }

    @JsonProperty
    public String data(){
        if(data == null) return "";
        return data.toString();
    }

    @JsonProperty
    public String signature(){
        if(signature == null) return "";
        return signature.toString();
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

	@Override
	public String toString(){
		return "TransactionData : { " + "hash='" + id().toString() +"'"+
				",  nonce=" + nonce +
				", gasPrice=" + ByteUtil.byteArrayToLong(gasPrice) +
				", gas=" + ByteUtil.byteArrayToLong(gasLimit) +
				", receiveAddress='" + to() +"'"+
				", sendAddress='" + from.toAddressString() +"'" +
				", value=" + amount +
				", data=" + data() +
				", signature='" + (signature == null ? "" : signature.toString())+"'" +
				"}";
	}
	public static Transaction deserialize(DataInputStream is) {
		try {
			val txType = TransactionType.getTransactionTypeByValue(is.readByte());
			val version = is.readInt();
			val from = UInt160.deserialize(is);
			val toPubKeyHash = UInt160.deserialize(is);
			val toName = Serializabler.readString(is);
			val amount = Fixed8.deserialize(is);
			UInt256 assetId = UInt256.deserialize(is);
			val nonce = is.readLong();
			val data = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
            val gasPrice = Serializabler.readByteArray(is);
            val gasLimit = Serializabler.readByteArray(is);
			val signature = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
			val id = UInt256.deserialize(is);
			return new Transaction(txType, from, toPubKeyHash, toName, amount, assetId, nonce, data, signature, version,
					id,gasPrice,gasLimit);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

    public static Writes<Transaction> writes(){
        Writes<Transaction> transactionWrites = new Writes<Transaction>() {
            @Override
            public JsValue writes(Transaction o) {
                return Json.parse(o.writes(o));
            }
        };

        return transactionWrites;
    }

    public byte[] getSender() {
        return from.getData();
    }

    public boolean isContractCreation() {
			return (this.txType  == TransactionType.Contract || this.txType  == TransactionType.Estimate_ContractConsumer)
                    && (this.toPubKeyHash == null || contractAddressEmpty(this.toPubKeyHash));
    }

	public boolean contractAddressEmpty(UInt160 toPubKeyHash){
		return toPubKeyHash.compare(UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20)) == 0;
	}


//	public UInt160 getContractAddress(){
//		if(!isContractCreation()) return null;
//		BinaryData privKeyBin = CryptoUtil.array2binaryData(Crypto.randomBytes(32));
//		PrivateKey privKey = PrivateKey.apply(privKeyBin);
//		return privKey.publicKey().pubKeyHash();
//	}

	public PrivateKey getContractAccount(){
		if(!isContractCreation()) return null;
		BinaryData privKeyBin = CryptoUtil.array2binaryData(Crypto.randomBytes(32));
		PrivateKey privKey = PrivateKey.apply(privKeyBin);
		return privKey;
	}
	public UInt160 getContractAddress() {
		if (!isContractCreation()) return null;
		byte[] addr = HashUtil.calcNewAddr(this.getSender(), longToBytes(nonce));
		return new UInt160(DataWord.of(addr).getLast20Bytes());
	}

    public byte[] getReceiveAddress() {
        return toPubKeyHash.getData();
    }

    public BigInteger getValue() {
        return amount.getValue();
    }


    public byte[] getDatas() {
        return CryptoUtil.binaryData2array(data);
    }

    public long nonZeroDataBytes() {
	    byte[] datas = getDatas();
        if (datas == null) return 0;
        int counter = 0;
        for (final byte aData : datas) {
            if (aData != 0) ++counter;
        }
        return counter;
    }



    public Transaction(TransactionType txType, UInt160 from, UInt160 to, String toName, Fixed8 amount,
                       UInt256 assetId, Long nonce, BinaryData data, BinaryData signature, int version,
					   UInt256 id,byte[] gasPrice,byte[] gasLimit) {
        this.txType = txType;
        this.from = from;
        this.toPubKeyHash = to;
        this.toName = toName;
        this.amount = amount;
        this.assetId = assetId;
        this.nonce = nonce;
        this.data = data;
        this.signature = signature;
        this.version = 0x01;
        this.id = id;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
    }
	public long getTransactionCost(){
		long nonZeroes = nonZeroDataBytes();
		long zeroVals  = ArrayUtils.getLength(CryptoUtil.binaryData2array(getData())) - nonZeroes;

		return (isContractCreation() ? GasCost.getInstance().getTRANSACTION_CREATE_CONTRACT() : GasCost.getInstance().getTRANSACTION())
				+ zeroVals * GasCost.getInstance().getTX_ZERO_DATA() + nonZeroes * GasCost.getInstance().getTX_NO_ZERO_DATA();
	}
	public byte[] getEncoded() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		try {
			serializeExcludeId(os);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bs.toByteArray();
	}
}
