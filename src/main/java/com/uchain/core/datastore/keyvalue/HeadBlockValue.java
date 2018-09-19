package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;

import lombok.val;

public class HeadBlockValue implements Converter<HeadBlock> {

    @Override
    public byte[] toBytes(HeadBlock key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public HeadBlock fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return HeadBlock.deserialize(is);
    }

    @Override
    public HeadBlock deserializer(DataInputStream is) {
        return HeadBlock.deserialize(is);
    }

    @Override
    public void serializer(HeadBlock key, DataOutputStream os) {
        key.serialize(os);
    }

}
