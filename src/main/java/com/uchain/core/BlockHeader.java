package com.uchain.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.util.ArrayList;

@Getter
@Setter
public class BlockHeader implements Identifier<UInt160> {
    private int index;
    private long timeStamp;
    private UInt256 merkleRoot;
    private UInt256 prevBlock;
    private BinaryData producer;
    private BinaryData producerSig;
    private int version = 0x01;
    private UInt256 _id;

    public BlockHeader(int index, long timeStamp, UInt256 merkleRoot, UInt256 prevBlock, BinaryData producer,
                       BinaryData producerSig/*, int version*/) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.merkleRoot = merkleRoot;
        this.prevBlock = prevBlock;
        this.producer = producer;
        this.producerSig = producerSig;
        this.version = 0x01;
    }

    // public BlockHeader (int index, long timeStamp, UInt256 merkleRoot, UInt256
    // prevBlock,
    // BinaryData producer, PrivateKey privateKey, int version, UInt256 _id){
    // if(CryptoUtil.binaryData2array(producer).length == 33){
    // this.index = index;
    // this.timeStamp = timeStamp;
    // this.merkleRoot = merkleRoot;
    // this.prevBlock = prevBlock;
    // this.producer = producer;
    // this.producerSig = CryptoUtil.array2binaryData(BinaryData.empty);
    // this.version = version;
    // this._id = _id;
    // sign(privateKey);
    // }
    // }
    //
    // public BlockHeader (int index, long timeStamp, UInt256 merkleRoot, UInt256
    // prevBlock,
    // BinaryData producer, BinaryData producerSig, int version, UInt256 _id){
    // if(CryptoUtil.binaryData2array(producer).length == 33){
    // this.index = index;
    // this.timeStamp = timeStamp;
    // this.merkleRoot = merkleRoot;
    // this.prevBlock = prevBlock;
    // this.producer = producer;
    // this.producerSig = producerSig;
    // this.version = version;
    // this._id = _id;
    // }
    // }

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj.getClass() == getClass()) {
//			BlockHeader blk = (BlockHeader) obj;
//			return blk.id() == id();
//		}
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		return id().hashCode();
//	}

	@Override
	public void serialize(DataOutputStream os) {
		serializeExcludeId(os);
		Serializabler.write(os, id());
	}

	private UInt256 genId() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializeExcludeId(os);
		return UInt256Util.fromBytes(Crypto.hash256(bs.toByteArray()));
	}

	private void serializeForSign(DataOutputStream os) {
		try {
			os.writeInt(version);
			os.writeInt(index);
			os.writeLong(timeStamp);
			Serializabler.write(os, merkleRoot);
			Serializabler.write(os, prevBlock);
			Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(producer));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void serializeExcludeId(DataOutputStream os) {
		serializeForSign(os);
		try {
			Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(producerSig));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] getSigTargetData() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializeForSign(os);
		return bs.toByteArray();
	}

	public BinaryData sign(PrivateKey privKey) {
		producerSig = CryptoUtil.array2binaryData(Crypto.sign(getSigTargetData(), privKey));
		return producerSig;
	}

	public boolean verifySig() {
		return Crypto.verifySignature(getSigTargetData(), CryptoUtil.binaryData2array(producerSig),
				CryptoUtil.binaryData2array(producer));
	}

	@Override
	public UInt256 id() {
		if (_id == null) {
			_id = genId();
		}
		return _id;
	}

	
	public static BlockHeader build(int index, Long timeStamp, UInt256 merkleRoot, UInt256 prevBlock,
			BinaryData producer, PrivateKey privateKey) {
		if (CryptoUtil.binaryData2array(producer).length == 33) {
			BinaryData binaryData = CryptoUtil.seq2binaryData(new ArrayList<Byte>());
			val header = new BlockHeader(index, timeStamp, merkleRoot, prevBlock, producer, binaryData);
			header.sign(privateKey);
			return header;
		} else {
			return null;
		}
	}
	
	public static BlockHeader deserialize(DataInputStream is) throws IOException {
		int version = is.readInt();
		int index = is.readInt();
		long timeStamp = is.readLong();
		UInt256 merkleRoot = UInt256Util.deserialize(is);
		UInt256 prevBlock = UInt256Util.deserialize(is);
		BinaryData producer = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
		BinaryData producerSig = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
		return new BlockHeader(index, timeStamp, merkleRoot, prevBlock,producer,producerSig/*, version, UInt256Util.deserialize(is)*/);
	}

	public static BlockHeader fromBytes(byte[] data) throws IOException {
		val bs = new ByteArrayInputStream(data);
		val is = new DataInputStream(bs);
		return deserialize(is);
	}
	
	public static String writes(BlockHeader blockHeader) {
		BlockHeaderJson blockHeaderJson = new BlockHeaderJson(blockHeader.get_id().toString(),blockHeader.getIndex(),
				blockHeader.getTimeStamp(),blockHeader.getMerkleRoot().toString(),blockHeader.getPrevBlock().toString(),
				blockHeader.getProducer().toString(),blockHeader.getProducerSig().toString(),blockHeader.getVersion());
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(blockHeaderJson);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}
}
