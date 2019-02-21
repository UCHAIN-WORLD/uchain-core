package com.uchain;

import com.google.common.collect.Maps;
import com.uchain.core.Code;
import com.uchain.core.Contract;
import com.uchain.crypto.UInt160;
import com.uchain.vm.DataWord;
import lombok.val;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Map;

public class CodeSerializerTest {
    @Test
    public void testCode(){


        Code code = new Code(new byte[]{2,2,2,2,2,2}, new byte[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        code.serialize(os);
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        Code contractDer = code.deserialize(is);

        System.out.println("11111111111111");
    }
}
