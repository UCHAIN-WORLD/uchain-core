package com.uchain.core.datastore.keyvalue;

import com.uchain.core.Account;
import com.uchain.core.Contract;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ContractValue implements Converter<Contract> {

    @Override
    public byte[] toBytes(Contract key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public Contract fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        return Contract.deserialize(is);
    }

    @Override
    public Contract deserializer(DataInputStream is) {
        return Contract.deserialize(is);
    }

    @Override
    public void serializer(Contract key, DataOutputStream os) {
        key.serialize(os);
    }

}
