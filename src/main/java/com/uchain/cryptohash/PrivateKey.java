package com.uchain.cryptohash;

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
                return new PrivateKey(Scalar.apply(new BinaryData(data.getData().subList(0, 32))));
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
        return getScalar().toBin();
        /*if (isCompressed()) {
            byte a = 1;
            List<Byte> bt = getScalar().toBin().getData();
            bt.add(a);
            return new BinaryData(bt);
        } else {
            return getScalar().toBin();
        }*/
    }

    public String toWIF() {
        // always treat as compressed key, do NOT use uncompressed key
        //TODO return Wallet.privKeyToWIF(getScalar().toBin());
        byte[] data = new byte[34];
        data[0] = (byte)0x80;
        data[33] = (byte) 0x01;
        System.arraycopy(CryptoUtil.binaryData2array(scalar.toBin()), 0, data, 1, 32);
        return Base58Check.encode(data);
    }

    public static PrivateKey fromWIF(String wif){
        byte[] decode;
        decode = Base58Check.decode(wif);
        if(decode.length == 34){
            if(decode[33] == (byte) 0x01){
                byte[] priv = new byte[32];
                System.arraycopy(decode, 1, priv, 0, 32);
                return PrivateKey.apply(CryptoUtil.array2binaryData(priv));
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toBin().toString();
    }
}