package com.uchain.crypto;

import java.util.List;

public class PrivateKey {
    private Scalar scalar;
    private boolean compressed;

    public PrivateKey(){
    }

    public PrivateKey(Scalar scalar) {
        this.scalar = scalar;
    }

    public PrivateKey(Scalar scalar, boolean compressed) {
        this.scalar = scalar;
        this.compressed = compressed;
    }

    public Scalar getScalar() {
        return scalar;
    }

    public void setScalar(Scalar scalar) {
        this.scalar = scalar;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public static PrivateKey apply(BinaryData data) {
        if (data.getLength(data.getData()) == 32){
            return new PrivateKey(Scalar.apply(data));
        } else if (data.getLength(data.getData()) == 33) {
            if (data.getData().get(data.getData().size() - 1) == 1) {
                return new PrivateKey(Scalar.apply(new BinaryData(data.getData().subList(0, 33))));
            }
        }
        throw new IllegalArgumentException("BinaryData must be initialized with a 32/33 bytes value");
    }

    public static PrivateKey apply(BinaryData data, boolean compressed) {
        if(compressed){
            return new PrivateKey(Scalar.apply(new BinaryData(data.getData().subList(0, 33))), compressed);
        }else{
            return new PrivateKey(Scalar.apply(new BinaryData(data.getData().subList(0, 32))), compressed);
        }

    }

    public PublicKey publicKey() {

        return new PublicKey(getScalar().toPoint(), true);
    }

    public BinaryData toBin() {

        if (isCompressed()) {
            byte a = 1;
            List<Byte> bt = getScalar().toBin().getData();
            bt.add(a);
            return new BinaryData(bt);
        } else {
            return getScalar().toBin();
        }
    }

    public String toWIF() {
        // always treat as compressed key, do NOT use uncompressed key
        //TODO return Wallet.privKeyToWIF(getScalar().toBin());
        return "";
    }

//    @Override
//    public String toString() {
//        return toBin().getData().toString();
//    }
}