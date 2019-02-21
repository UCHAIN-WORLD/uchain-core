package com.uchain;

import com.google.common.collect.Maps;
import com.uchain.common.Serializabler;
import com.uchain.core.Contract;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.vm.DataWord;
import lombok.val;
import org.junit.Test;

import java.io.*;
import java.util.Map;

public class SerializerTest2 {

    @Test
    public void Test256() throws IOException {
        UInt256 value = SerializerTest.testHash256();
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.write(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        UInt256 test256 = UInt256.deserialize(is);
        System.out.println("after:==>"+test256);

        assert (value.equals(test256));

    }

    @Test
    public void Test160() throws IOException{
        UInt160 value = SerializerTest.testHash160();
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.write(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        UInt160 test160 = UInt160.deserialize(is);
        System.out.println("after:==>"+test160);

        assert (value.equals(test160));

    }

    @Test
    public void TestMap()throws Exception{
        Map<UInt256, Fixed8> value = Maps.newLinkedHashMap();
        value.put(SerializerTest.testHash256(),new Fixed8(1000000000));
        value.put(SerializerTest.testHash256("Hello"),new Fixed8(2000000000));
        System.out.println("before:==>"+value);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        Serializabler.writeMap(os, value);

        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        Map<UInt256, Fixed8> testMap = Serializabler.readMap(is,true);
        System.out.println("after:==>"+testMap);

        for(Map.Entry obj:value.entrySet()){
            assert (testMap.get(obj.getKey()).equals(obj.getValue()));
        }
    }

}
