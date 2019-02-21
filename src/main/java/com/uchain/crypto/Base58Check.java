package com.uchain.crypto;


import java.util.ArrayList;
import java.util.Arrays;

public class Base58Check {

    public static String encode(byte prefix, byte []data) {
        byte []bt = new byte[1];
        bt[0] = prefix;

        return encode(bt, data);
    }

    public static String encode(byte []prefix, byte []data) {
        byte []bt = new byte[prefix.length + data.length];
        System.arraycopy(prefix, 0, bt, 0, prefix.length);
        System.arraycopy(data, 0, bt, prefix.length, data.length);
        return encode(bt);
    }

    public static String encode(byte []all) {
        byte []check = checksum(all);
        byte []bt = new byte[all.length + check.length];

        System.arraycopy(all, 0, bt, 0, all.length);
        System.arraycopy(check, 0, bt, all.length, check.length);

        return Base58.encode(bt);
    }

    public static byte[] decode(String input) {
        if (input.length() > 0) {
            byte []raw = Base58.decode(input);

            byte []versionAndHash = null;
            byte []checksum = null;

            if (raw.length > 4) {
                versionAndHash = CryptoUtil.listTobyte(CryptoUtil.byteToList(raw).subList(0, raw.length - 4));
                checksum = CryptoUtil.listTobyte(CryptoUtil.byteToList(raw).subList(raw.length - 4, raw.length));
                if (Arrays.equals(checksum(versionAndHash), checksum)) {
                    return versionAndHash;
                } else {
                    throw new IllegalArgumentException("invalid Base58Check data checksum");
                }
            } else {
                throw new IllegalArgumentException("Error input for Base58Check.decode");
            }
        }
        throw new IllegalArgumentException("Empty input for Base58Check.decode");
    }

    public static byte[] checksum(byte[] input) {
        byte [] bt = Crypto.hash256(input);
        ArrayList res = CryptoUtil.byteToList(bt);
        if (bt.length >= 4){
            return CryptoUtil.listTobyte(res.subList(0, 4));
        } else {
            return bt;
        }
    }
}
