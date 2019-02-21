package com.uchain.crypto.zksnark;

import java.math.BigInteger;

import static com.uchain.crypto.zksnark.Params.B_Fp2;


public class BN128Fp2 extends BN128<Fp2> {

    // the point at infinity
    static final BN128<Fp2> ZERO = new BN128Fp2(Fp2.ZERO, Fp2.ZERO, Fp2.ZERO);

    protected BN128Fp2(Fp2 x, Fp2 y, Fp2 z) {
        super(x, y, z);
    }

    @Override
    protected BN128<Fp2> zero() {
        return ZERO;
    }

    @Override
    protected BN128<Fp2> instance(Fp2 x, Fp2 y, Fp2 z) {
        return new BN128Fp2(x, y, z);
    }

    @Override
    protected Fp2 b() {
        return B_Fp2;
    }

    @Override
    protected Fp2 one() {
        return Fp2._1;
    }

    protected BN128Fp2(BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
        super(Fp2.create(a, b), Fp2.create(c, d), Fp2._1);
    }

    public static BN128<Fp2> create(byte[] aa, byte[] bb, byte[] cc, byte[] dd) {

        Fp2 x = Fp2.create(aa, bb);
        Fp2 y = Fp2.create(cc, dd);

        // check for point at infinity
        if (x.isZero() && y.isZero()) {
            return ZERO;
        }

        BN128<Fp2> p = new BN128Fp2(x, y, Fp2._1);

        // check whether point is a valid one
        if (p.isValid()) {
            return p;
        } else {
            return null;
        }
    }
}
