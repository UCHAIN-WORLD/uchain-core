package com.uchain;

import com.uchain.crypto.Fixed8;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2018/8/24.
 */
public class Fixed8Test {
    public static void main(String[] args){
        System.out.println(new Fixed8(1).toString().equals("0.00000001"));
        System.out.println(new Fixed8(10).toString().equals("0.00000010"));
        System.out.println(new Fixed8(111).toString().equals("0.00000111"));
        System.out.println(new Fixed8(100000000).toString().equals("1.00000000"));
        System.out.println(new Fixed8(100000001).toString().equals("1.00000001"));
        System.out.println(new Fixed8(100000101).toString().equals("1.00000101"));
        System.out.println(new Fixed8(100000100).toString().equals("1.00000100"));
        System.out.println(new Fixed8(1200000100).toString().equals("12.00000100"));
        System.out.println(new Fixed8(12300000100L).toString().equals("123.00000100"));
        System.out.println(new Fixed8(1234567800001100L).toString().equals("12345678.00001100"));
        System.out.println(new Fixed8(1234567891200001100L).toString().equals("12345678912.00001100"));
    }

    @Test
    public void testOperatorMinus() throws Exception {
        System.out.println(-Fixed8.MaxValue.getValue() == -Long.MAX_VALUE);
        System.out.println(-Fixed8.MinValue.getValue() == -Long.MIN_VALUE);
        System.out.println(-Fixed8.Zero.getValue() == 0);
        System.out.println(-new Fixed8(1).getValue() == -1);
    }

    @Test
    public void testOperatorEquals() throws Exception {
        System.out.println(Fixed8.MaxValue == Fixed8.MaxValue);
        System.out.println(Fixed8.MinValue == Fixed8.MinValue);
        System.out.println(Fixed8.One == Fixed8.One);
        System.out.println(Fixed8.Zero == Fixed8.Zero);
        System.out.println(!(Fixed8.MaxValue == Fixed8.MinValue));
        System.out.println(!(Fixed8.MinValue == Fixed8.One));
        System.out.println(!(Fixed8.One == Fixed8.Zero));
        System.out.println(new Fixed8(0) == new Fixed8(0));
        System.out.println(!(new Fixed8(0) == new Fixed8(1)));
    }

    @Test
    public void testEquals() throws Exception {
        System.out.println(Fixed8.MaxValue.equals(Fixed8.MaxValue));
        System.out.println(Fixed8.MinValue.equals(Fixed8.MinValue));
        System.out.println(Fixed8.One.equals(Fixed8.One));
        System.out.println(Fixed8.Zero.equals(Fixed8.Zero));
        System.out.println(!Fixed8.MaxValue.equals(Fixed8.MinValue));
        System.out.println(!Fixed8.MinValue.equals(Fixed8.One));
        System.out.println(!Fixed8.One.equals(Fixed8.Zero));
        System.out.println(new Fixed8(2018).equals(new Fixed8(2018)));
        System.out.println(!new Fixed8(2018).equals(new Fixed8(2017)));
        System.out.println(!new Fixed8(2018).equals(null));
    }

    @Test
    public void testSum() throws Exception {
        long[] arr1 = {new Fixed8(1).getValue(), new Fixed8(2).getValue(), new Fixed8(3).getValue()};
        long sum = 0;
        for (long i : arr1) {
            sum += i;
        }
        System.out.println(sum == new Fixed8(1 + 2 + 3).getValue());

        long sum2 = 0;
        long[] arr2 = {new Fixed8(-1).getValue(), new Fixed8(1).getValue()};
        for (long i: arr2) {
            sum2 += i;
        }
        System.out.println(sum2 == Fixed8.Zero.getValue());
    }

}