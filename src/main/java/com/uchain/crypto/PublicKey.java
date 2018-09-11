package com.uchain.crypto;

public class PublicKey {

    private Point point;
    private boolean compressed;

    public PublicKey(){

    }

    public PublicKey(Point point, boolean compressed){
        this.point = point;
        this.compressed = compressed;
    }

    public static PublicKey apply(BinaryData data){
        if (data.getData().size() == 65) {
            if (data.getData().get(0) == 4) {
                return new PublicKey(Point.apply(data), false);
            } else if (data.getData().get(0) == 6 || data.getData().get(0) == 7) {
                return new PublicKey(Point.apply(data), false);
            }
        } else if (data.getData().size() == 33) {
            if (data.getData().get(0) == 2 || data.getData().get(0) == 3){
                return new PublicKey(Point.apply(data), true);
            }

        }
        throw new IllegalArgumentException();
    }

    public BinaryData toBin() {
        return getPoint().toBin(compressed);
    }

    public UInt160 pubKeyHash(){
        return UInt160.fromBytes(hash160());
    }

    public byte[] hash160() {
         return Crypto.hash160(CryptoUtil.listTobyte(new PublicKey().toBin().getData()));
    }

    @Override
    public String toString() {
        return new PublicKey().getPoint().toString();
    }

//    public String toAddress() {
//    	return PublicKeyHash.toAddress(hash);
//    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}