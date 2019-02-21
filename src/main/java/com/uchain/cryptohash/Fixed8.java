package com.uchain.cryptohash;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Fixed8 implements Serializable {
    private BigInteger value;

    //    public static final Fixed8 MaxValue = new Fixed8(Long.MAX_VALUE);
//    public static final Fixed8 MinValue = new Fixed8(Long.MIN_VALUE);
    public static final Fixed8 One = new Fixed8(100000000);
    public static final Fixed8 Zero = new Fixed8(0);
    public static final Fixed8 Ten = new Fixed8(1000000000);

    public Fixed8() {
    }

    public Fixed8(BigInteger value) {
        this.value = value;
    }

    public Fixed8(long value) {
        this.value = BigInteger.valueOf(value);
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public Fixed8 ceiling() {
        BigInteger remainder = this.value.mod(Fixed8.One.getValue());
        if (remainder.longValue() == 0) {
            return this;
        } else if (remainder.longValue() > 0) {
            return new Fixed8(getValue().subtract(remainder).add(Fixed8.One.getValue()));
        } else {
            return new Fixed8(getValue().subtract(remainder));
        }
    }

    @Override
    public String toString() {
        BigDecimal big1 = new BigDecimal(getValue());
        BigDecimal big2 = new BigDecimal(Fixed8.One.getValue());

        return big1.divide(big2, 8, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString();
    }

    public boolean equals(Fixed8 that) {
        if (this == that) return true;
        if (that == null) return false;
        return this.value.compareTo(that.getValue()) == 0;
    }

    @Override
    public boolean equals(Object obj) {
        Object obj1 = obj;
        boolean flag;
        if (obj1 instanceof Fixed8) {
            Fixed8 fixed8 = (Fixed8) obj1;
            flag = equals(fixed8);
        } else {
            flag = false;
        }
        return flag;
    }

    @Override
    public void serialize(DataOutputStream os) {
        try {
            Serializabler.writeByteArray(os, getValue().toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Fixed8 deserialize(DataInputStream is) throws IOException {
        byte[] bytes = Serializabler.readByteArray(is);
        return new Fixed8(new BigInteger(bytes));
    }

    // -value
    public Fixed8 unary() {
        return new Fixed8(getValue().negate());
    }

    // +
    public Fixed8 add(Fixed8 that) {
        return new Fixed8(getValue().add(that.getValue()));
    }

    // -
    public Fixed8 mus(Fixed8 that) {
        return new Fixed8(getValue().subtract(that.getValue()));
    }

    // *
    public Fixed8 multiply(Fixed8 that) {
        return new Fixed8(getValue().multiply(that.getValue()));
    }

    // >
    public boolean greater(Fixed8 that) {
        return getValue().compareTo(that.getValue()) > 0;
    }

    // ==
    public boolean eq(Fixed8 that) {
        Fixed8 fixed8 = that;
        boolean flag;
        if (fixed8 == null) {
            flag = false;
        } else {
            flag = getValue() == that.getValue();
        }
        return flag;
    }

//    public static Fixed8 sum(List<Fixed8> list) {
//        BigInteger result = BigInteger.ZERO;
//        for (Fixed8 f8 : list) {
//            result = result.add(f8.getValue());
//        }
//
//        return new Fixed8(result);
//    }

//    public static Fixed8 min(List<Fixed8> list) {
//        long min = list.stream().mapToLong(Fixed8::getValue).min().getAsLong();
//        return new Fixed8(min);
//    }
//
//    public static Fixed8 max(List<Fixed8> list) {
//        long max = list.stream().mapToLong(Fixed8::getValue).max().getAsLong();
//        return new Fixed8(max);
//    }

    public static Fixed8 fromDecimal(BigDecimal d) {
        BigDecimal oneD = new BigDecimal(Fixed8.One.getValue());
        BigDecimal value = d.multiply(oneD);

        return new Fixed8(value.toBigInteger());
    }

    public static Fixed8 fromDouble(Double d) {
        BigDecimal bigD = BigDecimal.valueOf(d);
        BigDecimal oneD = new BigDecimal(Fixed8.One.getValue());
        BigDecimal value = bigD.multiply(oneD);

        return new Fixed8(value.toBigInteger());
    }

    public int compare(Fixed8 x, Fixed8 y) {
        if (x == null || y == null) {
            throw new IllegalArgumentException();
        }

        return x.getValue().compareTo(y.getValue());
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
