package com.uchain.crypto;

import org.bouncycastle.util.encoders.Hex;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;


public class UInt160 extends UIntBase /*implements Ordered*/ {

    public String toAddressString() {
        return PublicKeyHash.toAddress(getData());
    }

    public UInt160(byte []data) {
        super(UIntUtil.UInt160_Size, data);
    }

    public int compare(UInt160 that) {
        return UIntBaseCompare.compare(this, that);
    }

    public static UInt160 fromBytes(byte []bytes) {
        if (bytes.length == 20){
            return new UInt160(bytes);
        }
        throw new IllegalArgumentException();
    }

    public static UInt160 parse(String str) {
        if (str == null || str.isEmpty()){
            return null;
        } else {
            String s;
            if (str.startsWith("0x")){
                s = str.substring(2);
            } else {
                s = str;
            }
            if (s.length() != 40){
                return null;
            } else {
                return UInt160.fromBytes(Hex.decode(s));
            }
        }
    }

    public static UInt160 deserialize(DataInputStream is) {
        byte []data = new byte[UIntUtil.UInt160_Size];
        Arrays.fill(data, (byte)0);
        try {
            is.read(data, 0, UIntUtil.UInt160_Size);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return UInt160.fromBytes(data);
    }

    public static UInt160 Zero() {
        byte []data = new byte[UIntUtil.UInt160_Size];
        Arrays.fill(data, (byte)0);
        return UInt160.fromBytes(data);
    }


}
