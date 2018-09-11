package com.uchain.crypto;

import com.uchain.common.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class Fixed8 implements Serializable {
    private long value;

    public static final Fixed8 MaxValue = new Fixed8(Long.MAX_VALUE);
    public static final Fixed8 MinValue = new Fixed8(Long.MIN_VALUE);
    public static final Fixed8 One = new Fixed8(100000000);
    public static final Fixed8 Zero = new Fixed8(0);
    public static final Fixed8 Ten = new Fixed8(1000000000);

    public Fixed8(){
    }

    public Fixed8(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Fixed8 ceiling() {
        long remainder = getValue() % Fixed8.One.getValue();
        if(remainder == 0L){
            return this;
        } else if(remainder > 0) {
            return new Fixed8(getValue() - remainder + Fixed8.One.getValue());
        } else {
            return new Fixed8(getValue() - remainder);
        }
    }

    @Override
    public String toString() {
        long v = getValue() / Fixed8.One.getValue();
        long remain = getValue() % Fixed8.One.getValue() + Fixed8.One.getValue();
        return (new StringBuilder(1)).append(String.valueOf(v)).append(".").append(String.valueOf(remain).substring(1)).toString();
    }

    public boolean equals(Fixed8 that) {
        return this == that;
    }

    @Override
    public boolean equals(Object obj) {
        Object obj1 = obj;
        boolean flag;
        if(obj1 instanceof Fixed8) {
            Fixed8 fixed8 = (Fixed8)obj1;
            flag = equals(fixed8);
        } else {
            flag = false;
        }
        return flag;
    }

    @Override
    public void serialize(DataOutputStream os) {
        try {
            os.writeLong(getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -value
    public Fixed8 unary() {
        return new Fixed8(-getValue());
    }

    // +
    public Fixed8 add(Fixed8 that) {
        return new Fixed8(getValue() + that.getValue());
    }

    // -
    public Fixed8 mus(Fixed8 that) {
        return new Fixed8(getValue() - that.getValue());
    }

    // *
    public Fixed8 multiply(Fixed8 that) {
        return new Fixed8(getValue() * that.getValue());
    }

    // >
    public boolean greater(Fixed8 that) {
        return getValue() > that.getValue();
    }

    // ==
    public boolean eq(Fixed8 that) {
        Fixed8 fixed8 = that;
        boolean flag;
        if(fixed8 == null){
            flag = false;
        } else {
            flag = getValue() == that.getValue();
        }
        return flag;
    }

    public static Fixed8 sum(List<Fixed8> list) {
        long sum = list.stream().mapToLong(Fixed8 :: getValue).sum();
        return new Fixed8(sum);
    }

    public static Fixed8 min(List<Fixed8> list) {
        long min = list.stream().mapToLong(Fixed8 :: getValue).min().getAsLong();
        return new Fixed8(min);
    }

    public static Fixed8 max(List<Fixed8> list) {
        long max = list.stream().mapToLong(Fixed8 :: getValue).max().getAsLong();
        return new Fixed8(max);
    }

    public static Fixed8 deserialize(DataInputStream is) {
        long readlong = 0;
        try {
            readlong = is.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Fixed8(readlong);
    }

    public static Fixed8 fromDecimal(BigDecimal d) {
        long value = d.longValue() * Fixed8.One.getValue();
        try {
            if (value < Long.MIN_VALUE || value > Long.MAX_VALUE){
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Fixed8(value);
    }


    /*public static class Fixed8Numeric implements Numeric {

        public Fixed8 plus(Fixed8 x, Fixed8 y) {
            return x + y;
        }

        public Fixed8 minus(Fixed8 x, Fixed8 y) {
            return x - y;
        }

        public Fixed8 times(Fixed8 x, Fixed8 y) {
            return x * y;
        }

        public Fixed8 negate(Fixed8 x) {
            return -x;
        }

        public Fixed8 fromInt(int x) {
            return new Fixed8(x);
        }

        public int toInt(Fixed8 x) {
            return (int) x.value();
        }

        public long toLong(Fixed8 x) {
            return x.value();
        }

        public float toFloat(Fixed8 x) {
            return (float) x.value();
        }

        public double toDouble(Fixed8 x) {
            return (double) x.value();
        }

        public int compare(Fixed8 x, Fixed8 y) {
            if (x == null || y == null){
                throw new IllegalArgumentException();
            }

            return (x.getValue() < y.getValue()) ? -1 : ((x.getValue() == y.getValue()) ? 0 : 1);
        }

    }*/

    /*public static Fixed8 Zero() {
        return Fixed8.Zero();
    }

    public static Fixed8 One() {
        return Fixed8.One();
    }

    public static Fixed8 MinValue() {
        return Fixed8.MinValue();
    }

    public static Fixed8 MaxValue() {
        return Fixed8.MaxValue();
    }*/
}
