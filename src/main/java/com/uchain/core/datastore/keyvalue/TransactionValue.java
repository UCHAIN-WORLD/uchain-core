package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;

import lombok.val;

public class TransactionValue implements Converter<Transaction> {

    @Override
    public byte[] toBytes(Transaction key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public Transaction fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return Transaction.deserialize(is);
    }

    @Override
    public Transaction deserializer(DataInputStream is) {
        return Transaction.deserialize(is);
    }

    @Override
    public void serializer(Transaction key, DataOutputStream os) {
        key.serialize(os);
    }

}
