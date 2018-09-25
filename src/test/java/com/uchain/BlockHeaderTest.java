package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKey;
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
        val producer = PublicKey.apply(new BinaryData("022ac01a1ea9275241615ea6369c85b41e2016abc47485ec616c3c583f1b92a5c8"));
        val timeStamp = DateTime.now().clicks();
        val blockHeader = BlockHeader.build(0,timeStamp,merkleRoot,prevBlock,producer,PrivateKey.apply(new BinaryData("efc382ccc0358f468c2a80f3738211be98e5ae419fc0907cb2f51d3334001471")));
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
        assert(blockHeader.getProducer().getPoint().getValue().equals(blockHeaderDeserializer.getProducer().getPoint().getValue()));
        assert(blockHeader.getProducerSig().getData().equals(blockHeaderDeserializer.getProducerSig().getData()));
    }

    @Test
    public void test() throws IOException{
    	val prevBlock = SerializerTest.testHash256("prev");
        val merkleRoot = SerializerTest.testHash256("root");
        val producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));
        val timeStamp = DateTime.now().clicks();
        val blockHeader = new BlockHeader(0, timeStamp, merkleRoot, prevBlock, producer, new BinaryData("0000"),0x01,null);
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
