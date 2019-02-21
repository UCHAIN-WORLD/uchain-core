package com.uchain.cryptohash;

import org.bouncycastle.util.encoders.Hex;

public final class Base16 implements BytesEncoder{
    @Override
    public String Alphabet() {
        return "0123456789abcdefABCDEF";
    }

    @Override
    public String encode(byte[] input) {
        return Hex.toHexString(input);
    }

    @Override
    public byte[] decode(String input) {
        return  Hex.decode(input);
    }

}
