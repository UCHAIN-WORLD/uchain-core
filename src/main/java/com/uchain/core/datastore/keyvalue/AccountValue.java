package com.uchain.core.datastore.keyvalue;

import java.io.*;

import com.uchain.common.Serializabler;
import com.uchain.core.Account;

import lombok.val;

public class AccountValue implements Converter<Account> {

    @Override
    public byte[] toBytes(Account key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public Account fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return Account.deserialize(is);
    }

    @Override
    public Account deserializer(DataInputStream is) {
        return Account.deserialize(is);
    }

    @Override
    public void serializer(Account key, DataOutputStream os) {
        key.serialize(os);
    }

}
