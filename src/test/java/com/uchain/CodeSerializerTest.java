package com.uchain;

import com.uchain.core.Code;
import lombok.val;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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
