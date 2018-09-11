package com.uchain.crypto;

import java.util.List;

import org.bouncycastle.util.encoders.Hex;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BinaryData/* implements Serializable*/ {

    private List<Byte> data;

    public static byte[] empty = new byte[0];
    public BinaryData(String hex) {
        this.data = CryptoUtil.byteToList(Hex.decode(hex));
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
    public String toString() {
        return Hex.toHexString(CryptoUtil.listTobyte(getData()));
    }
}
