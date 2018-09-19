package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;
import com.uchain.core.BlkTxMapping;
import lombok.val;

public class BlkTxMappingValue implements Converter<BlkTxMapping> {

    @Override
    public byte[] toBytes(BlkTxMapping key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public BlkTxMapping fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return BlkTxMapping.deserialize(is);
    }

    @Override
    public BlkTxMapping deserializer(DataInputStream is) {
        return BlkTxMapping.deserialize(is);
    }

    @Override
    public void serializer(BlkTxMapping key, DataOutputStream os) {
        key.serialize(os);
    }

}
