package com.uchain;

import com.uchain.cryptohash.Fixed8;
import com.uchain.cryptohash.UInt160;
import lombok.val;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by Administrator on 2018/8/24.
 */
public class UInt160Test {
    @Test(expected = IllegalArgumentException.class)
    public void testCtorNull() throws Exception {
        new UInt160(null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testCtorWrongSize1() {
        byte[] data = new byte[15];
        Arrays.fill(data, (byte)0);
        new UInt160(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtorWrongSize2() {
        byte[] data = new byte[17];
        Arrays.fill(data, (byte)0);
        new UInt160(data);
    }
    @Test(expected = IllegalArgumentException.class)
    public void compare() throws Exception {
        byte []out = new byte[15];
        Arrays.fill(out, (byte)0);
        new UInt160(out);

    }

    @Test(expected = IllegalArgumentException.class)
    public void fromBytes() throws Exception {
        byte []out = new byte[17];
        Arrays.fill(out, (byte)0);
        new UInt160(out);
    }

    @Test
    public void testEquals() throws Exception {
        assert (!UInt160.Zero().equals(null));
        assert (UInt160.Zero().equals(UInt160.Zero()));
        val a = SerializerTest.testHash160();
        val b = SerializerTest.testHash160();
        val c = SerializerTest.testHash256();
        val d = SerializerTest.testHash160("Test");
        assert(a.equals(a));
        assert(a.equals(b));
        assert(!a.equals(c));
        assert(!a.equals(d));
        assert(!a.equals(null));
    }

    @Test
    public void testCompare() throws IOException{
        assert(UInt160.Zero().compare(UInt160.Zero()) == 0);
        val a = SerializerTest.testHash160();
        val b = SerializerTest.testHash160();
        val c = SerializerTest.testHash160("Test");
        assert(a.compare(b) == 0);
        assert(a.compare(c) < 0);
        assert(c.compare(a) > 0);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testCompareNull() throws Exception {
        UInt160.Zero().compare(null);
    }

    @Test
    public void zero() throws Exception {

    }

    @Test
    public void testSerialize() throws IOException {
        val bis = new ByteArrayInputStream(new byte[]{1,2});
        DataInputStream is = new DataInputStream(bis);
        val o = new SerializerTest(UInt160.deserialize(is),is);
        o.test(SerializerTest.testHash160());
        o.test(UInt160.Zero());
    }

    @Test
    public void testIdforJson(){
        String str = "{ status : 200 , result : { \"id\" : \"e1af7263e88d40e82d591fbab49fd6e1f6fc8c85f551609c544c0811f0c35f0d\" }}";
        JSONObject jb = JSONObject.fromObject(str);
        System.out.println(jb);
        BigDecimal bigDecimal = new BigDecimal("1.2");
        System.out.println(bigDecimal);
        System.out.println(Fixed8.fromDecimal(bigDecimal));
    }
}