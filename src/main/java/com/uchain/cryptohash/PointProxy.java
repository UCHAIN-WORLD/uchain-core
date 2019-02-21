package com.uchain.cryptohash;

public class PointProxy {
    private BinaryData data;

    public BinaryData getData() {
        return data;
    }

    public void setData(BinaryData data) {
        this.data = data;
    }

    public static Object readResolve(BinaryData data) {
        return Point.apply(data);
    }
}