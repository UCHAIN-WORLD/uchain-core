package com.uchain.cryptohash;



public interface BytesEncoder {

    public abstract String Alphabet();

    public abstract String encode(byte abyte0[]);

    public abstract byte[] decode(String s);
}
