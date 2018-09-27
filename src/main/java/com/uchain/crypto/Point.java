package com.uchain.crypto;

import com.uchain.common.Serializable;
import org.bouncycastle.math.ec.ECPoint;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;


public class Point implements Serializable{
    private ECPoint value;

    public Point(ECPoint value) {
        this.value = value;
    }

    public ECPoint getValue() {
        return value;
    }

    public void setValue(ECPoint value) {
        this.value = value;
    }

    public Point add(Point point) {
        return new Point(getValue().add(point.getValue()));
    }

    public Point substract(Point point) {
        return new Point(getValue().subtract(point.value));
    }

    public Point multiply(Scalar scalar) {
        return new Point(getValue().multiply(scalar.getValue()));
    }

    public void normalize() {
        new Point(getValue().normalize());
    }

    public BinaryData toBin(Boolean compressed) {
        return new BinaryData(CryptoUtil.byteToList(getValue().getEncoded(compressed)));
    }

    protected Object writeReplace() {
        return PointProxy.readResolve(toBin(true));
    }

    @Override
    public String toString() {
        return toBin(true).getData().toString();
    }

    public static Point apply(BinaryData data) {
        return new Point(new Ecdsa().getCurve().getCurve().decodePoint(CryptoUtil.listTobyte(data.getData())));
    }

    public static Point deserialize(DataInputStream is) {
        byte []data = new byte[0];
        try {
            data = new byte[is.readInt()];
            Arrays.fill(data, (byte)0);
            is.read(data, 0, data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return apply(new BinaryData(CryptoUtil.byteToList(data)));
    }

    @Override
    public void serialize(DataOutputStream os) {
        BinaryData data = toBin(true);
        try {
            os.write(data.getData().size());
            os.write(CryptoUtil.listTobyte(data.getData()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return Objects.equals(getValue(), point.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }
}
