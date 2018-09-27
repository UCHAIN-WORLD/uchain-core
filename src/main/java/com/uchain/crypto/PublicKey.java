package com.uchain.crypto;

import com.google.common.collect.Maps;
import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class PublicKey implements Serializable {

    private Point point;
    private boolean compressed;

    public PublicKey(){

    }

    public PublicKey(Point point, boolean compressed){
        this.point = point;
        this.compressed = compressed;
    }

    public static PublicKey apply(BinaryData data){
        if (data.getData().size() == 65) {
            if (data.getData().get(0) == 4) {
                return new PublicKey(Point.apply(data), false);
            } else if (data.getData().get(0) == 6 || data.getData().get(0) == 7) {
                return new PublicKey(Point.apply(data), false);
            }
        } else if (data.getData().size() == 33) {
            if (data.getData().get(0) == 2 || data.getData().get(0) == 3){
                return new PublicKey(Point.apply(data), true);
            }

        }
        throw new IllegalArgumentException();
    }

    public BinaryData toBin() {
        return getPoint().toBin(compressed);
    }

    public UInt160 pubKeyHash(){
        return UInt160.fromBytes(hash160());
    }

    public byte[] hash160() {
         return Crypto.hash160(CryptoUtil.listTobyte(toBin().getData()));
    }

//    @Override
//    public String toString() {
//        return new PublicKey().getPoint().toString();
//    }

    public String toAddress() {
    	return PublicKeyHash.toAddress(pubKeyHash().getData());
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public void serialize(DataOutputStream os) {
        try {
            Serializabler.writeByteArray(os,CryptoUtil.binaryData2array(toBin()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PublicKey deserialize(DataInputStream is) {
        try {
            return PublicKey.apply(CryptoUtil.array2binaryData(Serializabler.readByteArray(is)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PublicKey)) return false;
        PublicKey publicKey = (PublicKey) o;
        return isCompressed() == publicKey.isCompressed() &&
                Objects.equals(getPoint(), publicKey.getPoint());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPoint(), isCompressed());
    }

    public static void main(String[] args) {

        Map<PublicKey, Integer> lph = Maps.newLinkedHashMap();
        lph.put(PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682")),1);
        System.out.println(lph.get(PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"))));
        System.out.println(lph.containsKey(PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"))));
    }
}