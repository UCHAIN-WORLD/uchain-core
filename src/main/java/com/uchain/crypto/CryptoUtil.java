package com.uchain.crypto;

import org.bouncycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CryptoUtil {

    public static String toHexString(BinaryData blob) {
        return Hex.toHexString(binaryData2array(blob));
    }

    public static BinaryData fromHexString(String hex) {
        String str;
        if (hex.startsWith("0x")){
            str = hex.substring(2);
        } else {
            str = hex;
        }
        return new BinaryData(str);//array2binaryData(Hex.decode(str));
    }

    public BinaryData string2binaryData(String input) {
        return fromHexString(input);
    }

    public static BinaryData seq2binaryData(ArrayList<Byte> input) {
        return fromHexString(Hex.toHexString(listTobyte(input)));
    }

    public static BinaryData array2binaryData(byte []input) {
        return fromHexString(Hex.toHexString(input));
    }

    public static byte[] binaryData2array(BinaryData input) {

        return listTobyte(input.getData());
    }

    public List<Byte> binaryData2Seq(BinaryData input) {
        return input.getData();
    }

    public static byte[] listTobyte(List<Byte> list) {
        if (list == null || list.size() < 0){
            return null;
        }
        byte[] bytes = new byte[list.size()];
        int i = 0;
        Iterator<Byte> iterator = list.iterator();
        while (iterator.hasNext()) {
            bytes[i] = iterator.next();
            i++;
        }
        return bytes;
    }

    public static ArrayList<Byte> byteToList(byte []bt){
        ArrayList<Byte> b = new ArrayList<Byte>();

        for(int i = 0; i < bt.length; i++){
            b.add(bt[i]);
        }
        return b;
    }
    
    public static void main(String[] args) {
//    	BinaryData binaryData = new BinaryData("0000");
//    	byte[] aa = CryptoUtil.binaryData2array(binaryData);
    	BinaryData binaryData = CryptoUtil.seq2binaryData(CryptoUtil.byteToList(Hex.decode("0000")));
//    	System.out.println(binaryData.getHex());
//    	CryptoUtil.listTobyte(CryptoUtil.byteToList(Hex.decode("0000")));
	}

}
