package com.uchain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import play.api.libs.json.JsValue;
import play.api.libs.json.Json;
import play.api.libs.json.Writes;

import java.io.*;
import java.util.ArrayList;

@Getter
@Setter
public class BlockHeader implements Identifier<UInt160> {
    private int index;
    private long timeStamp;
    @JsonIgnore
    private UInt256 merkleRoot;
    @JsonIgnore
    private UInt256 prevBlock;
    @JsonIgnore
    private PublicKey producer;
    @JsonIgnore
    private BinaryData producerSig;
    private int version = 0x01;
    @JsonIgnore
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
			return blk.id().equals(id());
		}
		return false;
	}

	public String shortId() {
    	return id().toString().substring(0, 7);
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

	@JsonProperty
	public String merkleRoot(){
        return merkleRoot.toString();
    }

    @JsonProperty
    public String prevBlock(){
        return prevBlock.toString();
    }

    @JsonProperty
    public String producerSig(){
        return producerSig.toString();
    }

    @JsonProperty
    public String producer(){
        return producer.toString();
    }

    @JsonProperty("id")
    public String reJsonId(){
        return id.toString();
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

	public static Writes<BlockHeader> writes(){
        Writes<BlockHeader> blockHeaderWrites = new Writes<BlockHeader>() {
            @Override
            public JsValue writes(BlockHeader o) {
                return Json.parse("{\"id\" : "+ (o.id == null ? o.id : "\""+o.id.toString()+"\"")
                        + ", \"index\" : " + o.index
                        + ", \"timeStamp\" : " + o.timeStamp
                        + ", \"merkleRoot\" : " + (o.merkleRoot == null ? o.merkleRoot : "\""+o.merkleRoot.toString()+"\"")
                        + ", \"prevBlock\" : " +(o.prevBlock == null ? o.prevBlock : "\""+o.prevBlock.toString()+"\"")
                        + ", \"producer\" : " + (o.producer == null ? o.producer : "\""+o.producer.toString()+"\"")
                        + ", \"producerSig\" : " + (o.producerSig == null ? o.producerSig : "\""+o.producerSig.toString()+"\"")
                        + ", \"version\" : " + o.version
                        +"}");
            }
        };
        return blockHeaderWrites;
    }
}
