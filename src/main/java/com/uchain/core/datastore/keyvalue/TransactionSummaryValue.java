package com.uchain.core.datastore.keyvalue;

import com.uchain.common.Serializabler;
import com.uchain.core.TransactionReceipt;
import lombok.val;

import java.io.*;

public class TransactionSummaryValue implements Converter<TransactionReceipt> {
    @Override
    public byte[] toBytes(TransactionReceipt key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public TransactionReceipt fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return deserializer(is);
    }

    @Override
    public TransactionReceipt deserializer(DataInputStream is) {
        try {
            byte[] rlp = Serializabler.readByteArray(is);
            return new TransactionReceipt(rlp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void serializer(TransactionReceipt key, DataOutputStream os) {
        key.serialize(os);
    }
}
