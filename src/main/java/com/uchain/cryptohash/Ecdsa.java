package com.uchain.cryptohash;


import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class Ecdsa {

    private X9ECParameters params = SECNamedCurves.getByName("secp256k1");
    private ECDomainParameters curve = new ECDomainParameters(getParams().getCurve(), getParams().getG(), getParams().getN(), getParams().getH());
    private BigInteger halfCurveOrder = getParams().getN().shiftRight(1);
    private BigInteger zero = BigInteger.valueOf(0);
    private BigInteger one = BigInteger.valueOf(1);

    public X9ECParameters getParams() {
        return params;
    }

    public ECDomainParameters getCurve() {
        return curve;
    }

    public BigInteger getHalfCurveOrder() {
        return halfCurveOrder;
    }

    public BigInteger getZero() {
        return zero;
    }

    public BigInteger getOne() {
        return one;
    }

    public static BinaryData fixSize(BinaryData dt) {
        if (dt.getLength(dt.getData())== 32){
            return dt;
        } else if (dt.getLength(dt.getData()) < 32){
            byte []out = new byte[32-dt.getLength(dt.getData())];
            Arrays.fill(out, (byte)0);
            ArrayList<Byte> array = CryptoUtil.byteToList(out);
            array.addAll(dt.getData());
            return new BinaryData(array);
        }
        return null;
    }

    public static BinaryData hash(Digest digest, List<Byte> input) {
        digest.update(CryptoUtil.listTobyte(input), 0, input.size());
        byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return new BinaryData(CryptoUtil.byteToList(out));
    }


    public BinaryData encodeSignature(BigInteger r, BigInteger s) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(72);
        DERSequenceGenerator seq = null;
        try {
            seq = new DERSequenceGenerator(bos);
            seq.addObject(new ASN1Integer(r));
            seq.addObject(new ASN1Integer(s));
            seq.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BinaryData(CryptoUtil.byteToList(bos.toByteArray()));
    }

    public static BinaryData encodeSignature(List<BigInteger> t) {
        return new Ecdsa().encodeSignature(t.get(0), t.get(1));
    }

    public List<BigInteger> normalizeSignature(BigInteger r, BigInteger s) {
        BigInteger s1 = null;
        if (s.compareTo(halfCurveOrder) > 0) {
            s1 = curve.getN().subtract(s);
        } else {
            s1 = s;
        }
        List<BigInteger> list = new ArrayList<>();
        list.add(r);
        list.add(s);
        return list;
    }

    public BinaryData normalizeSignatureBinaryData(BinaryData sig) {
        List<BigInteger> list = decodeSignature(sig.getData());
        return encodeSignature(normalizeSignature(list.get(0), list.get(1)));
    }

    public boolean isPubKeyValid(List<Byte> key) {
        if (key.size() == 65 && (key.get(0) == 4 || key.get(0) == 6 || key.get(0) == 7)) {
            return true;
        } else if (key.size() == 65 && (key.get(0) == 2 || key.get(0) == 3)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPubKeyCompressedOrUncompressed(List<Byte> key) {
        if (key.size() == 65 && key.get(0) == 4) {
            return true;
        } else if (key.size() == 33 && (key.get(0) == 2 || key.get(0) == 3)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPubKeyCompressed(List<Byte> key) {
        if (key.size() == 33 && (key.get(0) == 2 || key.get(0) == 3)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPrivateKeyCompressed(PrivateKey key) {
        return key.isCompressed();
    }

    public List<BigInteger> decodeSignature(List<Byte> blob) {
        return decodeSignatureLax(new BinaryData(blob));
    }

    public List<BigInteger> decodeSignatureLax(ByteArrayInputStream input) {
        if (input.read() != 0x30) {
            throw new IllegalArgumentException();
        }

        readLength(input);

        if (input.read() != 0x02) {
            throw new IllegalArgumentException();
        }

        int lenR = readLength(input);

        byte[] r = new byte[lenR];
        try {
            input.read(r);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (input.read() != 0x02) {
            throw new IllegalArgumentException();
        }
        int lenS = readLength(input);
        byte[] s = new byte[lenS];
        try {
            input.read(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<BigInteger> list = new ArrayList<>();
        list.add(new BigInteger(1, r));
        list.add(new BigInteger(1, s));

        return list;
    }

    public int readLength(ByteArrayInputStream input) {
        int len = input.read();
        if ((len & 0x80) == 0) {
            return len;
        } else {
            int n = len - 0x80;
            int len1 = 0;
            while (n > 0) {
                len1 = (len1 << 8) + input.read();
                n = n - 1;
            }
            return len1;
        }
    }

    public List<BigInteger> decodeSignatureLax(BinaryData input) {
        return decodeSignatureLax(new ByteArrayInputStream(CryptoUtil.listTobyte(input.getData())));
    }

    public boolean verifySignature(List<Byte> data, List<BigInteger> signature, PublicKey publicKey) {
        return verifySignature(new BinaryData(data), encodeSignature(signature), publicKey);
    }

    public boolean verifySignature(BinaryData data, BinaryData signature, PublicKey publicKey) {
        List<BigInteger> list = decodeSignature(signature.getData());
        if (list.get(0).compareTo(getOne()) < 0 ) {
            throw new IllegalArgumentException("r must be >= 1");
        }
        if (list.get(0).compareTo(getCurve().getN()) >= 0) {
            throw new IllegalArgumentException("r must be < N");
        }
        if (list.get(1).compareTo(getOne()) < 0) {
            throw new IllegalArgumentException("s must be < N");
        }
        if (list.get(1).compareTo(getCurve().getN()) >= 0) {
            throw new IllegalArgumentException("s must be < N");
        }


        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(publicKey.getPoint().getValue(), getCurve());
        signer.init(false, params);
        return signer.verifySignature(CryptoUtil.listTobyte(data.getData()), list.get(0), list.get(1));
    }

    public PublicKey publicKeyFromPrivateKey(BinaryData privateKey) {
        return PrivateKey.apply(new BinaryData(privateKey.getData())).publicKey();
    }

    public static List<BigInteger> sign(BinaryData data, PrivateKey privateKey) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(privateKey.getScalar().getValue(), new Ecdsa().getCurve());
        signer.init(true, privateKeyParameters);
        BigInteger[] bt = signer.generateSignature(CryptoUtil.listTobyte(data.getData()));
        Map<BigInteger, BigInteger> map = new HashMap<>();
        List<BigInteger> list = new ArrayList<>();
        list.add(bt[0]);
        if (bt[1].compareTo(new Ecdsa().getHalfCurveOrder()) > 0) {
            list.add(new Ecdsa().getCurve().getN().subtract(bt[1]));
        } else {
            list.add(bt[1]);
        }
        return list;
    }

    public List<PublicKey> recoverPublicKey(BinaryData sig,byte[] message){
        return recoverPublicKey(decodeSignatureLax(sig), message);
    }

    public List<PublicKey> recoverPublicKey(List<BigInteger> sig,byte[] message){
        BigInteger r = sig.get(0);
        BigInteger s = sig.get(1);
        BigInteger m = new BigInteger(1, message);
        List<Point> ps = recoverPoint(r);
        ECPoint Q1 = (ps.get(0).multiply(s).getValue().
                subtract(curve.getG().multiply(m))).
                multiply(r.modInverse(curve.getN()));
        ECPoint Q2 = (ps.get(1).multiply(s).getValue()
                .subtract(curve.getG().multiply(m)))
                .multiply(r.modInverse(curve.getN()));
        List<PublicKey> ret = new ArrayList<>();
        ret.add(new PublicKey(new Point(Q1)));
        ret.add(new PublicKey(new Point(Q2)));
        return ret;
    }

    private List<Point> recoverPoint(BigInteger x) {
        ECFieldElement x1 = curve.getCurve().fromBigInteger(x);
        ECFieldElement square = x1.square().add(curve.getCurve().getA()).multiply(x1)
                .add(curve.getCurve().getB());
        ECFieldElement y1 = square.sqrt();
        ECFieldElement y2 = y1.negate();
        ECPoint R1 = curve.getCurve()
                .createPoint(x1.toBigInteger(), y1.toBigInteger()).normalize();
        ECPoint R2 = curve.getCurve()
                .createPoint(x1.toBigInteger(), y2.toBigInteger()).normalize();
        List<Point> ret = new ArrayList<>();
        if (y1.testBitZero()) {
            ret.add(new Point(R2));
            ret.add(new Point(R1));
        }else{
            ret.add(new Point(R1));
            ret.add(new Point(R2));
        }
        return ret;
    }
}
