package com.uchain.core.datastore.keyvalue;

import com.uchain.core.Code;
import com.uchain.core.Contract;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class CodeValue implements Converter<Code>{
    @Override
    public byte[] toBytes(Code key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public Code fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return Code.deserialize(is);
    }

    @Override
    public Code deserializer(DataInputStream is) {
        return Code.deserialize(is);
    }

    @Override
    public void serializer(Code key, DataOutputStream os) {
        key.serialize(os);
    }
}
