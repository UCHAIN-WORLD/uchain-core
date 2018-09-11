package com.uchain;

import com.uchain.crypto.UInt160;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2018/8/24.
 */
public class UInt160Test {
    @Test
    public void testCtorNull() throws Exception {
        new UInt160(null);
    }

    @Test
    public void compare() throws Exception {
        byte []out = new byte[15];
        Arrays.fill(out, (byte)0);
        new UInt160(out);

    }

    @Test
    public void fromBytes() throws Exception {
        byte []out = new byte[17];
        Arrays.fill(out, (byte)0);
        new UInt160(out);
    }

    @Test
    public void testEquals() throws Exception {
        System.out.println(!UInt160.Zero().equals(null));
        System.out.println(UInt160.Zero().equals(UInt160.Zero()));
    }

    @Test
    public void testCompareNull() throws Exception {
        UInt160.Zero().compare(null);
    }

    @Test
    public void zero() throws Exception {

    }

}