package com.uchain.cryptohash;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MerkleTree {

    private MerkleTreeNode rootNode = null;
    private int depth;

    public MerkleTree(){

    }
    public MerkleTree(List<UInt256> hashes){
        if (hashes.size() == 0) {
            throw new IllegalArgumentException();
        }
        List<MerkleTreeNode> list = hashes.stream().map(
                u -> {
                    return new MerkleTreeNode(u, null, null, null);
                }
        ).collect(Collectors.toList());
        rootNode = MerkleTree.build(list);
        MerkleTreeNode node = rootNode;
        while (node != null){
            node = node.getLeft();
            depth += 1;
        }
    }

    public static MerkleTreeNode build(List<MerkleTreeNode> leaves){
        if (leaves.size() == 0){
            throw new IllegalArgumentException("leaves");
        } else if (leaves.size() == 1){
            return leaves.get(0);
        }

        int size = (leaves.size() + 1)/2;
        List<MerkleTreeNode> parents = new ArrayList<MerkleTreeNode>(size);
        for (int i = 0; i < size; i++){
            parents.add(i, new MerkleTreeNode());
            parents.get(i).setLeft(leaves.get(i * 2));
            leaves.get(i * 2).setParent(parents.get(i));
            if ((i * 2 + 1) == leaves.size()){
                parents.get(i).setRight(parents.get(i).getLeft());
            } else {
                parents.get(i).setRight(leaves.get(i * 2 + 1));
                leaves.get(i * 2 + 1).setParent(parents.get(i));
            }
            parents.get(i).setHash(new UInt256(Crypto.hash256(byteMerger(parents.get(i).getLeft().getHash().getData(), parents.get(i).getRight().getHash().getData()))));
        }
        return build(parents);
    }

    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        int i=0;
        for(byte bt: bt1){
            bt3[i]=bt;
            i++;
        }

        for(byte bt: bt2){
            bt3[i]=bt;
            i++;
        }
        return bt3;
    }

    public static UInt256 root(List<UInt256> hashes){
        if (hashes == null || hashes.isEmpty()){
            throw new IllegalArgumentException();
        }
        if (hashes.size() == 1){
            return hashes.get(0);
        } else {
            return new MerkleTree(hashes).getRootNode().getHash();
        }
    }

    public static UInt256 nullRoot(){
        return UInt256.Zero();
    }

    public MerkleTreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(MerkleTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}