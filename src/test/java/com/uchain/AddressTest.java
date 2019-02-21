package com.uchain;

import com.uchain.cryptohash.*;
import lombok.val;
import lombok.var;
import org.junit.Test;

import java.io.IOException;
public class AddressTest {
    @Test
    public void testHashToAddress() throws IOException {
//20 bytes data

        val address1 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("0000000000000000000000000000000000000000")));
        //20 bytes data
        val address2 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("654a5851e9372b87810a8e60cdd2e7cfd80b6e31")));
        //20 bytes data
        val address3 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("ffffffffffffffffffffffffffffffffffffffff")));
        var privKey = PrivateKey.apply(new BinaryData("d95a24029a083e65563a438f987e810d2b662da748988069619b5124a74bd4c4"),false);//32 bytes or 33
        var pubKey = privKey.publicKey();
        var pubKeyHash = pubKey.pubKeyHash().getData();  // f54a5851e9372b87810a8e60cdd2e7cfd80b6e31
        val address4 = PublicKeyHash.toAddress(pubKeyHash);

        assert(address1.toString().equals("UCKuUkfSNEBTar5YawagKtVxvCQxhvrkaQx") );
        assert(address2.toString().equals("UCV944ctRWLtoxaqW6k4EHvESkNonTHdACY"));
        assert(address3.toString().equals("UCjF5jmjewMvTfWgfy11eNdEhpvDeX7AcRi"));

        assert(address4.toString().equals("UCTo3erAwNWi5vpwmQy8M7xckYu1uDqbgez"));
    }

    @Test
    public void testAddressToHash() {
        val hash = PublicKeyHash.fromAddress("UCYYrqRv4bdZdgB2j3KXwU5qMRiYHwezWqY");
        assert(hash.toString().equals(new BinaryData("8ab35551621d9bd232fbf2f08bccc3b8307403c0").toString()));
    }

    @Test
    public void testWIFtoPrivKey() {
        val privKey = PrivateKey.fromWIF("KyaC7SJxyVMkid2dYaEaPX2NcVDbxHas6BD7dZgAtmPqMxGqgKVz");
        assert(privKey.toBin().equals( new BinaryData("463614c5e9c0fb28eb6ee45c5971d6cb69c59381a426c463e1507a4fa8318ef8")));
    }

    @Test
    public void testPrivKeyToWIF() {
        val privKey = PrivateKey.apply(new BinaryData("31dd6b0db2cb1168d7f4c2c62483f793ee5d9541e138fac139fff82aa41bf497"));
        val wif = privKey.toWIF();
        assert(wif.equals("KxteB8drNvPf7Ek8piNDhXy2x7WLShSDAiBq69P6Y63myRCyf5MQ"));
    }

    @Test
    public void testKeyGen() {
        System.out.println("======");
        for (int i = 1; i<=10; i++) {
            System.out.println(i);
            val privateKey = PrivateKey.apply(CryptoUtil.array2binaryData(Crypto.randomBytes(32)));

            System.out.print("priv key raw:           ");  System.out.println(privateKey.toString());  // 32
            System.out.print("priv key WIF format:    ");  System.out.println(privateKey.toWIF());
            System.out.print("pub key (compressed):   ");  System.out.println(privateKey.publicKey().toString());  // 1 + 32
            System.out.print("pub key hash160:        ");  System.out.println(privateKey.publicKey().pubKeyHash().toString());
            System.out.print("Address:                ");  System.out.println(privateKey.publicKey().toAddress());
            System.out.println("======");
        }
    }
}
