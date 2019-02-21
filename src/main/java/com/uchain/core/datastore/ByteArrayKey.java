package com.uchain.core.datastore;

import com.uchain.common.Serializabler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class ByteArrayKey extends Serializabler {

    private byte[] bytes;

    public ByteArrayKey(byte[] bytes){
        this.bytes = bytes;
    }

    public byte[] getBytes(){
        return this.bytes;
    }
    public boolean equals(Object obj){
        if( obj == this){
            return true;
        }
        if (obj instanceof ByteArrayKey){
           return Arrays.equals(this.bytes, ((ByteArrayKey)obj).bytes);
        }
        return false;
    }

    public int hashCode(){
        return Arrays.hashCode(bytes);
    }

    public void serialize(DataOutputStream os) throws IOException {
        Serializabler.writeByteArray(os, bytes);
    }

}
