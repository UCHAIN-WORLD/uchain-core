package com.uchain.core.datastore.keyvalue;

import com.uchain.core.consensus.SwitchState;
import lombok.val;

import java.io.*;

public class SwitchStateValue implements Converter<SwitchState> {
    @Override
    public byte[] toBytes(SwitchState key) {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializer(key,os);
        return bs.toByteArray();
    }

    @Override
    public SwitchState fromBytes(byte[] bytes) {
        val bs = new ByteArrayInputStream(bytes);
        val is = new DataInputStream(bs);
        try{
            return SwitchState.deserialize(is);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SwitchState deserializer(DataInputStream is) {
        try{
            return SwitchState.deserialize(is);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void serializer(SwitchState key, DataOutputStream os) {
        key.serialize(os);
    }
}
