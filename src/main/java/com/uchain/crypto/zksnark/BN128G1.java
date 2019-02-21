package com.uchain.crypto.zksnark;

public class BN128G1 extends BN128Fp {

    BN128G1(BN128<Fp> p) {
        super(p.x, p.y, p.z);
    }

    @Override
    public BN128G1 toAffine() {
        return new BN128G1(super.toAffine());
    }

    public static BN128G1 create(byte[] x, byte[] y) {

        BN128<Fp> p = BN128Fp.create(x, y);

        if (p == null) return null;

        if (!isGroupMember(p)) return null;

        return new BN128G1(p);
    }

    private static boolean isGroupMember(BN128<Fp> p) {
        return true;
    }
}
