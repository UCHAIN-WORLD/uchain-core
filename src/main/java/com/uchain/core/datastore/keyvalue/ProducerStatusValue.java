package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;

import lombok.val;

public class ProducerStatusValue implements Converter<ProducerStatus> {

    @Override
    public byte[] toBytes(ProducerStatus key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public ProducerStatus fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return ProducerStatus.deserialize(is);
    }

    @Override
    public ProducerStatus deserializer(DataInputStream is) {
        return ProducerStatus.deserialize(is);
    }

    @Override
    public void serializer(ProducerStatus key, DataOutputStream os) {
        key.serialize(os);
    }

}
