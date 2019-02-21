package com.uchain.cryptohash;

import java.util.List;

import lombok.val;
import org.bouncycastle.util.encoders.Hex;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BinaryData/* implements Serializable*/ {

    private List<Byte> data;

    public static byte[] empty = new byte[0];

    public BinaryData(String hex) {
        val decodeData = Hex.decode(hex);
        this.data = CryptoUtil.byteToList(decodeData);
    }
    public BinaryData(List<Byte> data) {
        this.data = data;
    }

    public int getLength(List<Byte> data){
        int length = data.size();
        return length;
    }

    public int getLength(){
        int length = data.size();
        return length;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj instanceof BinaryData){
            this.data = ((BinaryData) obj).data;
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Hex.toHexString(CryptoUtil.listTobyte(data));
    }
}
