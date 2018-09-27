package com.uchain.crypto;

import com.uchain.common.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.util.encoders.Hex;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Getter
@Setter
public class UIntBase implements Serializable {
    private int size;
    private byte[] data;

    public UIntBase(int size, byte []data) {
        this.size = size;
        this.data = data;
        if(data == null || data.length != size){
            throw new IllegalArgumentException("data");
        } else {
            return;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIntBase)) return false;
        UIntBase uIntBase = (UIntBase) o;
        return getSize() == uIntBase.getSize() &&
                Arrays.equals(getData(), uIntBase.getData());
    }

    @Override
    public int hashCode() {
        return ByteBuffer.wrap(getData()).getInt();
    }

    @Override
    public String toString() {
        return Hex.toHexString(getData());
    }

    @Override
    public void serialize(DataOutputStream os) {
        try {
            os.write(getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars).toLowerCase();
    }
}
