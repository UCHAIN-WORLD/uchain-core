package com.uchain.crypto;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
}