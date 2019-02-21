package com.uchain.uvm;

import com.uchain.cryptohash.ECKey;
import com.uchain.cryptohash.HashUtil;
import com.uchain.cryptohash.bnsnark.*;
import com.uchain.main.SystemProperties;
import com.uchain.util.BIUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;

import static com.uchain.util.BIUtil.*;
import static com.uchain.util.ByteUtil.*;
import static com.uchain.uvm.VMUtils.getSizeInWords;


public class PrecompiledContracts {

    private static final ECRecover ecRecover = new ECRecover();
    private static final Sha256 sha256 = new Sha256();
    private static final Ripempd160 ripempd160 = new Ripempd160();
    private static final Identity identity = new Identity();
    private static final ModExp modExp = new ModExp();
    private static final BN128Addition altBN128Add = new BN128Addition();
    private static final BN128Multiplication altBN128Mul = new BN128Multiplication();
    private static final BN128Pairing altBN128Pairing = new BN128Pairing();

    private static final DataWord ecRecoverAddr =       DataWord.of("0000000000000000000000000000000000000000000000000000000000000001");
    private static final DataWord sha256Addr =          DataWord.of("0000000000000000000000000000000000000000000000000000000000000002");
    private static final DataWord ripempd160Addr =      DataWord.of("0000000000000000000000000000000000000000000000000000000000000003");
    private static final DataWord identityAddr =        DataWord.of("0000000000000000000000000000000000000000000000000000000000000004");
    private static final DataWord modExpAddr =          DataWord.of("0000000000000000000000000000000000000000000000000000000000000005");
    private static final DataWord altBN128AddAddr =     DataWord.of("0000000000000000000000000000000000000000000000000000000000000006");
    private static final DataWord altBN128MulAddr =     DataWord.of("0000000000000000000000000000000000000000000000000000000000000007");
    private static final DataWord altBN128PairingAddr = DataWord.of("0000000000000000000000000000000000000000000000000000000000000008");

    public static PrecompiledContract getContractForAddress(DataWord address, SystemProperties config) {

        if (address == null) return identity;
        if (address.equals(ecRecoverAddr)) return ecRecover;
        if (address.equals(sha256Addr)) return sha256;
        if (address.equals(ripempd160Addr)) return ripempd160;
        if (address.equals(identityAddr)) return identity;

        // Byzantium precompiles
        if (address.equals(modExpAddr) && config.isEip198()) return modExp;
        if (address.equals(altBN128AddAddr) && config.isEip213()) return altBN128Add;
        if (address.equals(altBN128MulAddr) && config.isEip213()) return altBN128Mul;
        if (address.equals(altBN128PairingAddr) && config.isEip212()) return altBN128Pairing;

        return null;
    }

    private static byte[] encodeRes(byte[] w1, byte[] w2) {

        byte[] res = new byte[64];

        w1 = stripLeadingZeroes(w1);
        w2 = stripLeadingZeroes(w2);

        System.arraycopy(w1, 0, res, 32 - w1.length, w1.length);
        System.arraycopy(w2, 0, res, 64 - w2.length, w2.length);

        return res;
    }

    public static abstract class PrecompiledContract {
        public abstract long getGasForData(byte[] data);

        public abstract Pair<Boolean, byte[]> execute(byte[] data);
    }

    public static class Identity extends PrecompiledContract {

        public Identity() {
        }

        @Override
        public long getGasForData(byte[] data) {

            // gas charge for the execution:
            // minimum 1 and additional 1 for each 32 bytes word (round  up)
            if (data == null) return 15;
            return 15 + getSizeInWords(data.length) * 3;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {
            return Pair.of(true, data);
        }
    }

    public static class Sha256 extends PrecompiledContract {


        @Override
        public long getGasForData(byte[] data) {

            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round  up)
            if (data == null) return 60;
            return 60 + getSizeInWords(data.length) * 12;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            if (data == null) return Pair.of(true, HashUtil.sha256(EMPTY_BYTE_ARRAY));
            return Pair.of(true, HashUtil.sha256(data));
        }
    }


    public static class Ripempd160 extends PrecompiledContract {


        @Override
        public long getGasForData(byte[] data) {

            // TODO #POC9 Replace magic numbers with constants
            // gas charge for the execution:
            // minimum 50 and additional 50 for each 32 bytes word (round  up)
            if (data == null) return 600;
            return 600 + getSizeInWords(data.length) * 120;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            byte[] result = null;
            if (data == null) result = HashUtil.ripemd160(EMPTY_BYTE_ARRAY);
            else result = HashUtil.ripemd160(data);

            return Pair.of(true, DataWord.of(result).getData());
        }
    }


    public static class ECRecover extends PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 3000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            byte[] h = new byte[32];
            byte[] v = new byte[32];
            byte[] r = new byte[32];
            byte[] s = new byte[32];

            DataWord out = null;

            try {
                System.arraycopy(data, 0, h, 0, 32);
                System.arraycopy(data, 32, v, 0, 32);
                System.arraycopy(data, 64, r, 0, 32);

                int sLength = data.length < 128 ? data.length - 96 : 32;
                System.arraycopy(data, 96, s, 0, sLength);

                ECKey.ECDSASignature signature = ECKey.ECDSASignature.fromComponents(r, s, v[31]);
                if (validateV(v) && signature.validateComponents()) {
                    out = DataWord.of(ECKey.signatureToAddress(h, signature));
                }
            } catch (Throwable any) {
            }

            if (out == null) {
                return Pair.of(true, EMPTY_BYTE_ARRAY);
            } else {
                return Pair.of(true, out.getData());
            }
        }

        private static boolean validateV(byte[] v) {
            for (int i = 0; i < v.length - 1; i++) {
                if (v[i] != 0) return false;
            }
            return true;
        }
    }

    /**
     * Computes modular exponentiation on big numbers
     *
     * format of data[] array:
     * [length_of_BASE] [length_of_EXPONENT] [length_of_MODULUS] [BASE] [EXPONENT] [MODULUS]
     * where every length is a 32-byte left-padded integer representing the number of bytes.
     * Call data is assumed to be infinitely right-padded with zero bytes.
     *
     * Returns an output as a byte array with the same length as the modulus
     */
    public static class ModExp extends PrecompiledContract {

        private static final BigInteger GQUAD_DIVISOR = BigInteger.valueOf(20);

        private static final int ARGS_OFFSET = 32 * 3; // addresses length part

        @Override
        public long getGasForData(byte[] data) {

            if (data == null) data = EMPTY_BYTE_ARRAY;

            int baseLen = parseLen(data, 0);
            int expLen  = parseLen(data, 1);
            int modLen  = parseLen(data, 2);

            byte[] expHighBytes = parseBytes(data, addSafely(ARGS_OFFSET, baseLen), Math.min(expLen, 32));

            long multComplexity = getMultComplexity(Math.max(baseLen, modLen));
            long adjExpLen = getAdjustedExponentLength(expHighBytes, expLen);

            // use big numbers to stay safe in case of overflow
            BigInteger gas = BigInteger.valueOf(multComplexity)
                    .multiply(BigInteger.valueOf(Math.max(adjExpLen, 1)))
                    .divide(GQUAD_DIVISOR);

            return isLessThan(gas, BigInteger.valueOf(Long.MAX_VALUE)) ? gas.longValue() : Long.MAX_VALUE;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            if (data == null)
                return Pair.of(true, EMPTY_BYTE_ARRAY);

            int baseLen = parseLen(data, 0);
            int expLen  = parseLen(data, 1);
            int modLen  = parseLen(data, 2);

            BigInteger base = parseArg(data, ARGS_OFFSET, baseLen);
            BigInteger exp  = parseArg(data, addSafely(ARGS_OFFSET, baseLen), expLen);
            BigInteger mod  = parseArg(data, addSafely(addSafely(ARGS_OFFSET, baseLen), expLen), modLen);

            // check if modulus is zero
            if (isZero(mod))
                return Pair.of(true, EMPTY_BYTE_ARRAY);

            byte[] res = stripLeadingZeroes(base.modPow(exp, mod).toByteArray());

            // adjust result to the same length as the modulus has
            if (res.length < modLen) {

                byte[] adjRes = new byte[modLen];
                System.arraycopy(res, 0, adjRes, modLen - res.length, res.length);

                return Pair.of(true, adjRes);

            } else {
                return Pair.of(true, res);
            }
        }

        private long getMultComplexity(long x) {

            long x2 = x * x;

            if (x <= 64)    return x2;
            if (x <= 1024)  return x2 / 4 + 96 * x - 3072;

            return x2 / 16 + 480 * x - 199680;
        }

        private long getAdjustedExponentLength(byte[] expHighBytes, long expLen) {

            int leadingZeros = numberOfLeadingZeros(expHighBytes);
            int highestBit = 8 * expHighBytes.length - leadingZeros;

            // set index basement to zero
            if (highestBit > 0) highestBit--;

            if (expLen <= 32) {
                return highestBit;
            } else {
                return 8 * (expLen - 32) + highestBit;
            }
        }

        private int parseLen(byte[] data, int idx) {
            byte[] bytes = parseBytes(data, 32 * idx, 32);
            return DataWord.of(bytes).intValueSafe();
        }

        private BigInteger parseArg(byte[] data, int offset, int len) {
            byte[] bytes = parseBytes(data, offset, len);
            return bytesToBigInteger(bytes);
        }
    }

    public static class BN128Addition extends PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 500;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            if (data == null)
                data = EMPTY_BYTE_ARRAY;

            byte[] x1 = parseWord(data, 0);
            byte[] y1 = parseWord(data, 1);

            byte[] x2 = parseWord(data, 2);
            byte[] y2 = parseWord(data, 3);

            BN128<Fp> p1 = BN128Fp.create(x1, y1);
            if (p1 == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> p2 = BN128Fp.create(x2, y2);
            if (p2 == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> res = p1.add(p2).toEthNotation();

            return Pair.of(true, encodeRes(res.x().bytes(), res.y().bytes()));
        }
    }

    public static class BN128Multiplication extends PrecompiledContract {

        @Override
        public long getGasForData(byte[] data) {
            return 40000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            if (data == null)
                data = EMPTY_BYTE_ARRAY;

            byte[] x = parseWord(data, 0);
            byte[] y = parseWord(data, 1);

            byte[] s = parseWord(data, 2);

            BN128<Fp> p = BN128Fp.create(x, y);
            if (p == null)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            BN128<Fp> res = p.mul(BIUtil.toBI(s)).toEthNotation();

            return Pair.of(true, encodeRes(res.x().bytes(), res.y().bytes()));
        }
    }

    public static class BN128Pairing extends PrecompiledContract {

        private static final int PAIR_SIZE = 192;

        @Override
        public long getGasForData(byte[] data) {

            if (data == null) return 100000;

            return 80000 * (data.length / PAIR_SIZE) + 100000;
        }

        @Override
        public Pair<Boolean, byte[]> execute(byte[] data) {

            if (data == null)
                data = EMPTY_BYTE_ARRAY;

            // fail if input len is not a multiple of PAIR_SIZE
            if (data.length % PAIR_SIZE > 0)
                return Pair.of(false, EMPTY_BYTE_ARRAY);

            PairingCheck check = PairingCheck.create();

            // iterating over all pairs
            for (int offset = 0; offset < data.length; offset += PAIR_SIZE) {

                Pair<BN128G1, BN128G2> pair = decodePair(data, offset);

                // fail if decoding has failed
                if (pair == null)
                    return Pair.of(false, EMPTY_BYTE_ARRAY);

                check.addPair(pair.getLeft(), pair.getRight());
            }

            check.run();
            int result = check.result();

            return Pair.of(true, DataWord.of(result).getData());
        }

        private Pair<BN128G1, BN128G2> decodePair(byte[] in, int offset) {

            byte[] x = parseWord(in, offset, 0);
            byte[] y = parseWord(in, offset, 1);

            BN128G1 p1 = BN128G1.create(x, y);

            // fail if point is invalid
            if (p1 == null) return null;

            // (b, a)
            byte[] b = parseWord(in, offset, 2);
            byte[] a = parseWord(in, offset, 3);

            // (d, c)
            byte[] d = parseWord(in, offset, 4);
            byte[] c = parseWord(in, offset, 5);

            BN128G2 p2 = BN128G2.create(a, b, c, d);

            // fail if point is invalid
            if (p2 == null) return null;

            return Pair.of(p1, p2);
        }
    }
}
