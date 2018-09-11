package com.uchain.crypto;

public class UInt256 extends UIntBase {
    public UInt256(byte[] data) {
		super(UIntUtil.UInt256_Size, data);
	}

    public int compare(Object that) {
        return compare((UInt256)that);
    }
    public int compare(UInt256 that) {
        return UIntBaseCompare.compare(this, that);
    }
}
