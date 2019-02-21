package com.uchain;


import com.uchain.cryptohash.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/8/23.
 */
public class CryptoTest {
    @Test
    public void hash256() throws Exception {
        byte[] data = "abc".getBytes("US-ASCII");
        byte[] hash = Crypto.hash256(data);
        System.out.println(CryptoUtil.byteToList(hash));
        System.out.println(new BinaryData("4F8B42C22DD3729B519BA6F68D2DA7CC5B2D606D05DAED5AD5128CC03E6C6358").getData());
    }

    @Test
    public void hash160() throws Exception {
        BinaryData key = new BinaryData("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352");
        byte[] hash = Crypto.hash160(CryptoUtil.listTobyte(key.getData()));
        System.out.println(CryptoUtil.byteToList(hash));

        System.out.println(new BinaryData("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31").getData());
    }

    @Test
    public void sha256() throws Exception {
        BinaryData key = new BinaryData("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352");
        byte[] hash = Crypto.sha256(CryptoUtil.listTobyte(key.getData()));
        System.out.println(CryptoUtil.byteToList(hash));

        System.out.println(new BinaryData("0b7c28c9b7290c98d7438e70b3d3f7c848fbd7d1dc194ff83f4f7cc9b1378e98").getData());
    }

    @Test
    public void RIPEMD160() throws Exception {
        byte[] data = "Rosetta Code".getBytes("US-ASCII");
        byte[] hash = Crypto.RIPEMD160(data);
        System.out.println(CryptoUtil.byteToList(hash));

        System.out.println(new BinaryData("b3be159860842cebaa7174c8fff0aa9e50a5199f").getData());
    }

    @Test
    public void testEcdsaKey() throws Exception {
        PrivateKey privKey = PrivateKey.apply(new BinaryData("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725"));
        PublicKey pubKey = privKey.publicKey();

        System.out.println(pubKey.toBin().getData());

        System.out.println(new BinaryData("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352").getData());
    }

    @Test
    public void sign() throws Exception {
        BinaryData privKey = new BinaryData("f8b8af8ce3c7cca5e300d33939540c10d45ce001b8f252bfbc57ba0342904181");
        byte[] message = "Alan Turing".getBytes("US-ASCII");
        byte[] sig = Crypto.sign(message, CryptoUtil.listTobyte(privKey.getData()));
        System.out.println(CryptoUtil.byteToList(sig));

        System.out.println(new BinaryData("304402207063ae83e7f62bbb171798131b4a0564b956930092b33b07b395615d9ec7e15c022058dfcc1e00a35e1572f366ffe34ba0fc47db1e7189759b9fb233c5b05ab388ea").getData());
    }

    @Test
    public void verifySignature() throws Exception {
        byte[] message = "Alan Turing".getBytes("US-ASCII");

        BinaryData sig = new  BinaryData("304402207063ae83e7f62bbb171798131b4a0564b956930092b33b07b395615d9ec7e15c022058dfcc1e00a35e1572f366ffe34ba0fc47db1e7189759b9fb233c5b05ab388ea");

        // 32+1=33 bytes compressed pub key
        BinaryData pubKey = new BinaryData("0292df7b245b81aa637ab4e867c8d511008f79161a97d64f2ac709600352f7acbc");

        List<PublicKey> pubs1 = new Ecdsa().recoverPublicKey(sig, Crypto.sha256(message));
        List<PublicKey> pubs2 = Crypto.recoverPublicKey(sig, message);

        assert(Crypto.verifySignature(message, CryptoUtil.listTobyte(sig.getData()), CryptoUtil.listTobyte(pubKey.getData())));
        assert(Crypto.verifySignature(message, CryptoUtil.listTobyte(sig.getData()), CryptoUtil.binaryData2array(pubs1.get(0).toBin())));
        assert(Crypto.verifySignature(message, CryptoUtil.listTobyte(sig.getData()), CryptoUtil.binaryData2array(pubs1.get(1).toBin())));
        assert(Crypto.verifySignature(message, CryptoUtil.listTobyte(sig.getData()), CryptoUtil.binaryData2array(pubs2.get(0).toBin())));
        assert(Crypto.verifySignature(message, CryptoUtil.listTobyte(sig.getData()), CryptoUtil.binaryData2array(pubs2.get(1).toBin())));
        assert(Crypto.verifySignature(message, sig));
    }
    /*@Test
    def testSign2 = {
            val privKey = Ecdsa.PrivateKey(BinaryData("f8b8af8ce3c7cca5e300d33939540c10d45ce001b8f252bfbc57ba0342904181"))
            val pubkey = privKey.publicKey
            val message = "Alan Turing".getBytes("US-ASCII")
            val sig = Crypto.sign(message, privKey)
    assert(sig sameElements BinaryData("304402207063ae83e7f62bbb171798131b4a0564b956930092b33b07b395615d9ec7e15c022058dfcc1e00a35e1572f366ffe34ba0fc47db1e7189759b9fb233c5b05ab388ea"))

}*/
    @Test
    public void testRecoverPublicKey() {
        Random random = new Random();
        byte[] privbytes = new byte[32];
        byte[] message = new byte[32];
        for (int i =0;i< 10;i++) {
        random.nextBytes(privbytes);
        random.nextBytes(message);

        PrivateKey priv = PrivateKey.apply(new BinaryData(CryptoUtil.byteToList(privbytes)));
        PublicKey pub = priv.publicKey();
        List<BigInteger> pubrs = new Ecdsa().sign(new BinaryData(CryptoUtil.byteToList(message)), priv);
        BinaryData sigBin = new Ecdsa().encodeSignature(pubrs.get(0), pubrs.get(1));
        List<PublicKey> pubs2 = new Ecdsa().recoverPublicKey(sigBin, message);
        List<PublicKey> pubs = new Ecdsa().recoverPublicKey(pubrs, message);

        assert(new Ecdsa().verifySignature(CryptoUtil.byteToList(message), pubrs, pubs.get(0)));
        assert(new Ecdsa().verifySignature(CryptoUtil.byteToList(message), pubrs, pubs.get(1)));
        assert(pub.equals(pubs.get(0)) || pub.equals(pubs.get(1)));
        assert(pub.equals(pubs2.get(0)) || pub.equals(pubs2.get(1)));
        }
        }
    @Test
    public void testBase58() {
        //  00  f54a5851e9372b87810a8e60cdd2e7cfd80b6e31  c7f18fe8
        BinaryData data = new BinaryData("00f54a5851e9372b87810a8e60cdd2e7cfd80b6e31c7f18fe8");
        String res = Base58.encode(CryptoUtil.listTobyte(data.getData()));
        System.out.println(res);
//        assert(Base58.encode(data) == "1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs")

        byte[] dd = Base58.decode("1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs");
        System.out.println(CryptoUtil.byteToList(dd));

        BinaryData data1 = new BinaryData("00f54a5851e9372b87810a8e60cdd2e7cfd80b6e31c7f18fe8");
        System.out.println(data1.getData());
    }

    @Test
    public void testBase58Check() {
        BinaryData data = new BinaryData("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31");
        String res= Base58Check.encode( (byte)0x00, CryptoUtil.listTobyte(data.getData()));
        System.out.println(res);
//    assert(Base58Check.encode(0x00.toByte, data) == "1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs")

        //  00  f54a5851e9372b87810a8e60cdd2e7cfd80b6e31
        byte[] dd = Base58Check.decode("1PMycacnJaSqwwJqjawXBErnLsZ7RkXUAs");
        System.out.println(CryptoUtil.byteToList(dd));

        BinaryData data1 = new BinaryData("00f54a5851e9372b87810a8e60cdd2e7cfd80b6e31");
        System.out.println(data1.getData());
    }

    @Test
    public void testAes() {
        BinaryData key = new BinaryData("140b41b62a29beb4061bdd66b6747e14");
        BinaryData iv  = new BinaryData("20814805c1767293bd9f1d9cab2bc3e7");

        BinaryData data1 = new BinaryData("12345678");
        byte[] encrypted1 = Crypto.AesEncrypt(CryptoUtil.listTobyte(data1.getData()), CryptoUtil.listTobyte(key.getData()), CryptoUtil.listTobyte(iv.getData()));
        System.out.println(CryptoUtil.byteToList(encrypted1));
        System.out.println(new BinaryData("8B536DD84217046497B0EDA6AF72837A").getData());

        byte[] dec = Crypto.AesDecrypt(encrypted1, CryptoUtil.listTobyte(key.getData()), CryptoUtil.listTobyte(iv.getData()));
        System.out.println(CryptoUtil.byteToList(dec));
        System.out.println(data1.getData());
    }

    @Test
    public void testPointSerialize() {

        PrivateKey privKey = PrivateKey.apply(new BinaryData("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725"));
        PublicKey pubKey = privKey.publicKey();

        System.out.println(pubKey.toBin().getData());
        System.out.println(new BinaryData("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352").getData());

//    assert(pubKey.toBin.data.toArray sameElements BinaryData("0250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352"))

        Point point  = pubKey.getPoint();

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bs);
//        point.serialize(os);

        System.out.println(CryptoUtil.byteToList(bs.toByteArray()));
        System.out.println(new BinaryData("210250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352").getData());

        // 21 02 50863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352
        // 0x21 = 33 = (32 + 1) bytes
        // 0x02 : compressed type
//            assert(bs.toByteArray sameElements BinaryData("210250863ad64a87ae8a2fe83c1af1a8403cb53f53e486d8511dad8a04887e5b2352"))
    }

}