package com.uchain.cryptohash;


public class MerkleTreeNode {

    private UInt256 hash;
    private MerkleTreeNode parent;
    private MerkleTreeNode left;
    private MerkleTreeNode right;

    public MerkleTreeNode(){

    }

    public MerkleTreeNode(UInt256 hash, MerkleTreeNode parent, MerkleTreeNode left, MerkleTreeNode right) {
        this.hash = hash;
        this.parent = parent;
        this.left = left;
        this.right = right;
    }

    public boolean isRoot(){
        return parent == null;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public void setHash(UInt256 hash) {
        this.hash = hash;
    }

    public void setParent(MerkleTreeNode parent) {
        this.parent = parent;
    }

    public void setLeft(MerkleTreeNode left) {
        this.left = left;
    }

    public void setRight(MerkleTreeNode right) {
        this.right = right;
    }

    public UInt256 getHash() {

        return hash;
    }

    public MerkleTreeNode getParent() {
        return parent;
    }

    public MerkleTreeNode getLeft() {
        return left;
    }

    public MerkleTreeNode getRight() {
        return right;
    }
}
