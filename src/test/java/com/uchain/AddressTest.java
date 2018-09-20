/*
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: AddressTest.java
 *
 * @author: bridge.bu@chinapex.com: @version: 1.0
 */

package com.uchain;

import com.uchain.crypto.CryptoUtil;
import lombok.var;
import org.junit.Test;

import java.io.IOException;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PublicKeyHash;
import com.uchain.crypto.PrivateKey;
import lombok.val;
public class AddressTest {
    @Test
    public void testHashToAddress() throws IOException {
//20 bytes data

        val address1 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("0000000000000000000000000000000000000000")));
        //20 bytes data
        val address2 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("654a5851e9372b87810a8e60cdd2e7cfd80b6e31")));

        //20 bytes data
        val address3 = PublicKeyHash.toAddress(CryptoUtil.binaryData2array(new BinaryData("ffffffffffffffffffffffffffffffffffffffff")));

        var privKey = PrivateKey.apply(new BinaryData("18e14a7b6a307f426a94f8114701e7c8e774e7f9a47e2c2035db29a206321725"),false);//32 bytes or 33
        var pubKey = privKey.publicKey();
        var pubKeyHash = pubKey.pubKeyHash().getData();  // f54a5851e9372b87810a8e60cdd2e7cfd80b6e31
        val address4 = PublicKeyHash.toAddress(pubKeyHash);

        assert(address1.toString().equals("AP1xWDozWvuVah1W86DKtcWzdw1LLHreMGX") );
        assert(address2.toString().equals("APBC5XmSaD4vooWo3FNho1wGAUyBQo3WCTQ"));
        assert(address3.toString().equals("APRJ7CvHoe5xTWSeD7dfD6eGRZWbGomzDi4"));

        assert(address4.toString().equals("APQKUqPcJEUwRdwoxpoGQnkrRGstSXkgebk"));
    }
}
