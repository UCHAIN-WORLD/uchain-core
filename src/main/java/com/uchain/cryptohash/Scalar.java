package com.uchain.cryptohash;

import java.math.BigInteger;
import java.util.List;

public class Scalar {

    private BigInteger value;


    public Scalar(BigInteger value){
        this.value = value;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public static Scalar apply(BinaryData data){
        if (data.getLength(data.getData()) == 32){
            return new Scalar(new BigInteger(1, CryptoUtil.binaryData2array(data)));
        }
        throw new IllegalArgumentException("scalar must be initialized with a 32 bytes value");

    }

    public Scalar add(Scalar scalar){
        return new Scalar(new Scalar(getValue().add(scalar.getValue())).getValue().mod(new Ecdsa().getCurve().getN()));
    }

    public Scalar substract(Scalar scalar) {
        return new Scalar(getValue().subtract(scalar.getValue()).mod(new Ecdsa().getCurve().getN()));
    }

    public Scalar multiply(Scalar scalar) {
        return new Scalar(value.multiply(scalar.value).mod(new Ecdsa().getCurve().getN()));
    }

    public boolean isZero() {
        return getValue().equals(BigInteger.ZERO);
    }

    public BinaryData toBin() {
        byte[] value = getValue().toByteArray();

        int index = 0;
        for (int i = 0; i < value.length - 1; i ++) {
            if (value[i] == 0){
                index = i + 1;
            }else break;
        }

        List<Byte> bt = CryptoUtil.byteToList(value).subList(index, value.length);
        return Ecdsa.fixSize(new BinaryData(bt));
    }

    public Point toPoint() {
        return new Point(new Point(new Ecdsa().getParams().getG()).getValue().multiply(getValue()));
    }

    @Override
    public String toString() {
        return toBin().toString();
    }





}