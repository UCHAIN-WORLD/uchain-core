package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import lombok.val;

public class SerializerTest<A extends Serializable> {


    static boolean eqComparer(Serializable x, Serializable y){
        return x.equals(y);
    }

    @Test
    public void test(Account value) throws IOException{
        val bos = new ByteArrayOutputStream();
        val os = new DataOutputStream(bos);
        Serializabler.write(os, value);
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        val accountDeserializer = Account.deserialize(is);

    }

    public static UInt256 testHash256(String str) throws IOException {
        return UInt256.fromBytes(Crypto.hash256(str.getBytes("UTF-8")));
    }
    //java方法没有默认值，以此代替
    public static  UInt256 testHash256() throws IOException{
        String  str = "test";
        return UInt256.fromBytes(Crypto.hash256(str.getBytes("UTF-8")));

    }

    public static UInt160 testHash160(String str) throws IOException {
        return UInt160.fromBytes(Crypto.hash160(str.getBytes("UTF-8")));
    }
    //java方法没有默认值，以此代替
    public static UInt160 testHash160() throws IOException {
        String str = "test";
        return UInt160.fromBytes(Crypto.hash160(str.getBytes("UTF-8")));
    }

}
