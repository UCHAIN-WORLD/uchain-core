package com.uchain.uvm.repository;

import com.uchain.core.Account;
import com.uchain.core.block.BlockChain;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.uvm.ContractDetails;
import com.uchain.uvm.DataWord;
import com.uchain.uvm.Repository;

import java.math.BigInteger;
import java.util.Set;

public class RepositoryWrapper implements Repository {

    BlockChain blockchain;

    public RepositoryWrapper(BlockChain chain){
        this.blockchain = chain;
    }

    @Override
    public Account createAccount(UInt160 addr) {
        return blockchain.getRepository().createAccount(addr);
    }

    @Override
    public boolean isExist(UInt160 addr) {
        return blockchain.getRepository().isExist(addr);
    }

    @Override
    public Account getAccount(UInt160 addr) {
        return blockchain.getRepository().getAccount(addr);
    }

    @Override
    public void delete(UInt160 addr) {
        blockchain.getRepository().delete(addr);
    }

    @Override
    public Long increaseNonce(UInt160 addr) {
        return blockchain.getRepository().increaseNonce(addr);
    }

    @Override
    public Long setNonce(UInt160 addr, Long nonce) {
        return blockchain.getRepository().setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(UInt160 addr) {
        return blockchain.getRepository().getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(UInt160 addr) {
        return blockchain.getRepository().getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(UInt160 addr) {
        return blockchain.getRepository().hasContractDetails(addr);
    }

    @Override
    public void saveCode(UInt160 addr, byte[] code) {
        blockchain.getRepository().saveCode(addr, code);
    }

    @Override
    public byte[] getCode(UInt160 addr) {
        return blockchain.getRepository().getCode(addr);
    }

    @Override
    public byte[] getCodeHash(UInt160 addr) {
        return blockchain.getRepository().getCodeHash(addr);
    }

    @Override
    public void addStorageRow(UInt160 addr, DataWord key, DataWord value) {
        blockchain.getRepository().addStorageRow(addr, key, value);
    }

    @Override
    public DataWord getStorageValue(UInt160 addr, DataWord key) {
        return blockchain.getRepository().getStorageValue(addr, key);
    }

    @Override
    public void transfer(UInt160 fromAddr, UInt160 toAddr, BigInteger value) {
        blockchain.getRepository().transfer(fromAddr,toAddr,value);
    }

    @Override
    public BigInteger getBalance(UInt160 addr, UInt256 assetId) {
        return blockchain.getRepository().getBalance(addr, assetId);
    }

    @Override
    public BigInteger addBalance(UInt160 addr, BigInteger value, UInt256 assetId) {
        return blockchain.getRepository().addBalance(addr, value, assetId);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return blockchain.getRepository().getAccountsKeys();
    }

    @Override
    public Repository startTracking() {
        return this;
    }

    @Override
    public void commit() {
        blockchain.getRepository().commit();
    }

    @Override
    public void rollback() {
        blockchain.getRepository().rollback();
    }

    @Override
    public Repository clone() {
        return blockchain.getRepository();
    }
}
