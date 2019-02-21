package com.uchain.core.datastore.keyvalue;

import com.uchain.core.Block;
import lombok.val;

import java.io.*;

public class BlockValue implements Converter<Block>{

    @Override
    public byte[] toBytes(Block key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public Block fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        try {
            return Block.deserialize(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Block deserializer(DataInputStream is) {
        try {
            return Block.deserialize(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void serializer(Block key, DataOutputStream os) {
        key.serialize(os);
    }
}
