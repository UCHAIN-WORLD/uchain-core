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

@Getter
@Setter
public class BlockHeader implements Identifier<UInt160> {
    private int index;
    private long timeStamp;
    private UInt256 merkleRoot;
    private UInt256 prevBlock;
    private PublicKey producer;
    private BinaryData producerSig;
    private int version = 0x01;
    private UInt256 id;

    public BlockHeader(int index, long timeStamp, UInt256 merkleRoot, UInt256 prevBlock, PublicKey producer,
                       BinaryData producerSig, int version,UInt256 id) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.merkleRoot = merkleRoot;
        this.prevBlock = prevBlock;
        this.producer = producer;
        this.producerSig = producerSig;
        this.version = 0x01;
        this.id = id;
    }


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj.getClass() == getClass()) {
			BlockHeader blk = (BlockHeader) obj;
			return blk.id() == id();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id().hashCode();
	}

	@Override
	public void serialize(DataOutputStream os) {
		serializeExcludeId(os);
		Serializabler.write(os, id());
	}

	private UInt256 genId() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializeExcludeId(os);
		return UInt256.fromBytes(Crypto.hash256(bs.toByteArray()));
	}

	private void serializeForSign(DataOutputStream os) {
		try {
			os.writeInt(version);
			os.writeInt(index);
			os.writeLong(timeStamp);
			Serializabler.write(os, merkleRoot);
			Serializabler.write(os, prevBlock);
			Serializabler.write(os,producer);
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

	public void sign(PrivateKey privKey) {
		producerSig = CryptoUtil.array2binaryData(Crypto.sign(getSigTargetData(), privKey));
	}

	public boolean verifySig() {
		return Crypto.verifySignature(getSigTargetData(), CryptoUtil.binaryData2array(producerSig), CryptoUtil.binaryData2array(producer.toBin()));
	}

	@Override
	public UInt256 id() {
		if (id == null) {
			id = genId();
		}
		return id;
	}

	
	public static BlockHeader build(int index, Long timeStamp, UInt256 merkleRoot, UInt256 prevBlock,
									PublicKey producer, PrivateKey privateKey) {
		if (producer.toBin().getLength() == 33) {
			BinaryData binaryData = CryptoUtil.seq2binaryData(new ArrayList<Byte>());
			val header = new BlockHeader(index, timeStamp, merkleRoot, prevBlock, producer, binaryData,0x01,null);
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
		UInt256 merkleRoot = UInt256.deserialize(is);
		UInt256 prevBlock = UInt256.deserialize(is);
		PublicKey producer = PublicKey.deserialize(is);
		BinaryData producerSig = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
		return new BlockHeader(index, timeStamp, merkleRoot, prevBlock,producer,producerSig, version, UInt256.deserialize(is));
	}

	public static BlockHeader fromBytes(byte[] data) throws IOException {
		val bs = new ByteArrayInputStream(data);
		val is = new DataInputStream(bs);
		return deserialize(is);
	}
	
	public static String writes(BlockHeader blockHeader) {
		BlockHeaderJson blockHeaderJson = new BlockHeaderJson(blockHeader.getId().toString(),blockHeader.getIndex(),
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
