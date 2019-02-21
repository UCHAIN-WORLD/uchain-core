package com.uchain;

import com.uchain.crypto.Fixed8;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2018/8/24.
 */
public class Fixed8Test {

    @Test
    public void testToString() {
        assertEquals("0.00000001", new Fixed8(1).toString());
        assertEquals("0.0000001", new Fixed8(10).toString());
        assertEquals("0.00000111", new Fixed8(111).toString());
        assertEquals("1", new Fixed8(100000000).toString());
        assertEquals("1.00000001", new Fixed8(100000001).toString());
        assertEquals("1.00000101", new Fixed8(100000101).toString());
        assertEquals("1.000001", new Fixed8(100000100).toString());
        assertEquals("12.000001", new Fixed8(1200000100).toString());
        assertEquals("123.000001", new Fixed8(12300000100L).toString());
        assertEquals("12345678.000011", new Fixed8(1234567800001100L).toString());
        assertEquals("12345678912.000011", new Fixed8(1234567891200001100L).toString());
    }

    @Test
    public void testOperatorMinus() throws Exception {
        assertEquals(0, -Fixed8.Zero.getValue().intValue());
        assertEquals(-1, -new Fixed8(1).getValue().intValue());
    }

    @Test
    public void testOperatorEquals() throws Exception {
        assertTrue(Fixed8.One == Fixed8.One);
        assertTrue(Fixed8.Zero == Fixed8.Zero);
        assertTrue(!(Fixed8.One == Fixed8.Zero));
        assertTrue(!(new Fixed8(0) == new Fixed8(1)));
        assertTrue(Fixed8.One.equals(new Fixed8(100000000L)));
        assertTrue(Fixed8.One.equals(Fixed8.fromDouble(1.0)));
    }


}