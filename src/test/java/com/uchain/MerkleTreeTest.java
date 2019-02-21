package com.uchain;

import com.uchain.cryptohash.BinaryData;
import com.uchain.cryptohash.CryptoUtil;
import com.uchain.cryptohash.MerkleTree;
import com.uchain.cryptohash.UInt256;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/8/24.
 */
public class MerkleTreeTest {

    @Test
    public void testMerkleTree() {

        UInt256 h1 = new UInt256(CryptoUtil.listTobyte(new BinaryData("6b51f4b3bc2ca9611f91e58fb2960ccec30b70b6f1f8da59a67af5c04bf215a8").getData()));
        UInt256 h2 = new UInt256(CryptoUtil.listTobyte(new BinaryData("d1f38fd2017f6c536a689db5736d775fe312717aef87993b87cbb6f8364d779e").getData()));
        UInt256 h3 = new UInt256(CryptoUtil.listTobyte(new BinaryData("5f7d106852d44d4b842cd034c0ce4b47e5ab3752d79b012306bb16df4131b4c5").getData()));
        UInt256 h4 = new UInt256(CryptoUtil.listTobyte(new BinaryData("ca3d02f7d8bb600c5fcaa107d2587b5c527f9b3a97d90328ee633d3d25680931").getData()));
        UInt256 h5 = new UInt256(CryptoUtil.listTobyte(new BinaryData("3a77b92e05391baa0defc5993a2eb44c2b5627ac780c3bc67a4a58421a0e1f05").getData()));

        List<UInt256> list = new ArrayList<>();
        list.add(h1);
        System.out.println(new MerkleTree().root(list).toString().equals("6b51f4b3bc2ca9611f91e58fb2960ccec30b70b6f1f8da59a67af5c04bf215a8"));

        list.add(h2);
        System.out.println(new MerkleTree().root(list).toString().equals("06267b944009f5c0fe39e3d4d6bd646b2130ce430e5a979cd02fa193b477efb1"));

        list.add(h3);
        System.out.println(new MerkleTree().root(list).toString().equals("2cfe3fa489e89ce5c4b64bcc27a5b41cd2f23b940c8238cad963477c28f3fb98"));

        list.add(h4);
        System.out.println(new MerkleTree().root(list).toString().equals("c2237cc814f33840f0d900293539ae5aa7f1526155de4d05642c1f4a16572a06"));

        list.add(h5);
        System.out.println(new MerkleTree().root(list).toString().equals("a8e16367797f4a27275dd69607819764685890b8ec1ebec7a06303f5e6693a4c"));

//        assert(MerkleTree.root(Seq[UInt256](h1)).toString == "6b51f4b3bc2ca9611f91e58fb2960ccec30b70b6f1f8da59a67af5c04bf215a8")
//        assert(MerkleTree.root(Seq[UInt256](h1, h2)).toString == "06267b944009f5c0fe39e3d4d6bd646b2130ce430e5a979cd02fa193b477efb1")
//        assert(MerkleTree.root(Seq[UInt256](h1, h2, h3)).toString == "2cfe3fa489e89ce5c4b64bcc27a5b41cd2f23b940c8238cad963477c28f3fb98")
//        assert(MerkleTree.root(Seq[UInt256](h1, h2, h3, h4)).toString == "c2237cc814f33840f0d900293539ae5aa7f1526155de4d05642c1f4a16572a06")
//        assert(MerkleTree.root(Seq[UInt256](h1, h2, h3, h4, h5)).toString == "a8e16367797f4a27275dd69607819764685890b8ec1ebec7a06303f5e6693a4c")

    }
}