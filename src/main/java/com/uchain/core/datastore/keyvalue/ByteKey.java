package com.uchain.core.datastore.keyvalue;

import com.uchain.common.Serializabler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ByteKey implements Converter<byte[]> {
    @Override
    public byte[] toBytes(byte[] key) {
        return key;
    }

    @Override
    public byte[] fromBytes(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] deserializer(DataInputStream is) {
        try {
            Serializabler.readByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void serializer(byte[] key, DataOutputStream os) {
        try {
            Serializabler.writeByteArray(os,key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
