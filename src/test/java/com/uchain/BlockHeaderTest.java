package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.uchain.common.Serializabler;
import com.uchain.core.BlockHeader;
import com.uchain.core.BlockHeaderJson;
import com.uchain.crypto.BinaryData;

import akka.http.javadsl.model.DateTime;
import lombok.val;

public class BlockHeaderTest {
    @Test
    public void testSerialize() throws IOException{
        val prevBlock = SerializerTest.testHash256("prev");
        val merkleRoot = SerializerTest.testHash256("root");
        val producer = new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"); // TODO: read from settings
        val timeStamp = DateTime.now().clicks();
        val blockHeader = new BlockHeader(0, timeStamp, merkleRoot, prevBlock, producer, new BinaryData("0000")/*, 0x01, null*/);

        val bos = new ByteArrayOutputStream();
        val os = new DataOutputStream(bos);
        Serializabler.write(os, blockHeader);
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        val blockHeaderDeserializer = BlockHeader.deserialize(is);

        assert(blockHeader.getIndex() == blockHeaderDeserializer.getIndex());
        assert(blockHeader.getTimeStamp() == blockHeaderDeserializer.getTimeStamp());
        assert(blockHeader.getMerkleRoot().toString().equals(blockHeaderDeserializer.getMerkleRoot().toString()));
        assert(blockHeader.getPrevBlock().toString().equals(blockHeaderDeserializer.getPrevBlock().toString()));
        assert(blockHeader.getProducer().toString().equals(blockHeaderDeserializer.getProducer().toString()));
        assert(blockHeader.getProducerSig().toString().equals(blockHeaderDeserializer.getProducerSig().toString()));
    }

    @Test
    public void test() throws IOException{
    	val prevBlock = SerializerTest.testHash256("prev");
        val merkleRoot = SerializerTest.testHash256("root");
        val producer = new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"); 
        val timeStamp = DateTime.now().clicks();
        val blockHeader = new BlockHeader(0, timeStamp, merkleRoot, prevBlock, producer, new BinaryData("0000"));
        val bos = new ByteArrayOutputStream();
        val os = new DataOutputStream(bos);
        Serializabler.write(os, blockHeader);
        
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        val blockHeaderDeserializer = BlockHeader.deserialize(is);
        
        String jsonString = BlockHeader.writes(blockHeader);
        BlockHeaderJson blockHeaderJson = Serializabler.JsonMapperFrom(jsonString, BlockHeaderJson.class);
        assert(blockHeaderJson.getIndex() == blockHeaderDeserializer.getIndex());
        assert(blockHeaderJson.getTimeStamp() == blockHeaderDeserializer.getTimeStamp());
        assert(blockHeaderJson.getMerkleRoot().toString().equals(blockHeaderDeserializer.getMerkleRoot().toString()));
        assert(blockHeaderJson.getPrevBlock().toString().equals(blockHeaderDeserializer.getPrevBlock().toString()));
        assert(blockHeaderJson.getProducer().toString().equals(blockHeaderDeserializer.getProducer().toString()));
        assert(blockHeaderJson.getProducerSig().toString().equals(blockHeaderDeserializer.getProducerSig().toString()));
    }
}
