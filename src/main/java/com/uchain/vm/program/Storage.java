package com.uchain.vm.program;

import com.uchain.core.Account;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.vm.ContractDetails;
import com.uchain.vm.DataWord;
import com.uchain.vm.Repository;
import com.uchain.vm.program.invoke.ProgramInvoke;
import com.uchain.vm.program.listener.ProgramListener;
import com.uchain.vm.program.listener.ProgramListenerAware;

import java.math.BigInteger;
import java.util.Set;

public class Storage implements Repository, ProgramListenerAware {
    private final Repository repository;
    private final DataWord address;
    private ProgramListener programListener;

    public Storage(ProgramInvoke programInvoke) {
        this.address = programInvoke.getOwnerAddress();
        this.repository = programInvoke.getRepository();
    }

    private Storage(Repository repository, DataWord address) {
        this.repository = repository;
        this.address = address;
    }

    @Override
    public void setProgramListener(ProgramListener listener) {
        this.programListener = listener;
    }

    @Override
    public Account createAccount(UInt160 addr) {
        return repository.createAccount(addr);
    }

    @Override
    public boolean isExist(UInt160 addr) {
        return repository.isExist(addr);
    }

    @Override
    public Account getAccount(UInt160 addr) {
        return repository.getAccount(addr);
    }

    @Override
    public void delete(UInt160 addr) {
//        if (canListenTrace(addr)) programListener.onStorageClear();
//        repository.delete(addr);
    }

    @Override
    public Long increaseNonce(UInt160 addr) {
        return repository.increaseNonce(addr);
    }

    @Override
    public Long setNonce(UInt160 addr, Long nonce) {
        return repository.setNonce(addr, nonce);
    }

    @Override
    public BigInteger getNonce(UInt160 addr) {
        return repository.getNonce(addr);
    }

    @Override
    public ContractDetails getContractDetails(UInt160 addr) {
        return repository.getContractDetails(addr);
    }

    @Override
    public boolean hasContractDetails(UInt160 addr) {
        return repository.hasContractDetails(addr);
    }

    @Override
    public void saveCode(UInt160 addr, byte[] code) {
        repository.saveCode(addr, code);
    }

    @Override
    public byte[] getCode(UInt160 addr) {
        return repository.getCode(addr);
    }

    @Override
    public byte[] getCodeHash(UInt160 addr) {
        return repository.getCodeHash(addr);
    }

    @Override
    public void addStorageRow(UInt160 addr, DataWord key, DataWord value) {
        if (canListenTrace(addr)) programListener.onStoragePut(key, value);
        repository.addStorageRow(addr, key, value);
    }

    private boolean canListenTrace(UInt160 address) {
        return (programListener != null) && this.address.equals(DataWord.of(address.getData()));
    }

    @Override
    public DataWord getStorageValue(UInt160 addr, DataWord key) {
        return repository.getStorageValue(addr, key);
    }

    @Override
    public void transfer(UInt160 fromAddr, UInt160 toAddr, BigInteger value) {
        repository.transfer(fromAddr,toAddr,value);
    }

    @Override
    public BigInteger getBalance(UInt160 addr, UInt256 assetId) {
        return repository.getBalance(addr, assetId);
    }

    @Override
    public BigInteger addBalance(UInt160 addr, BigInteger value, UInt256 assetID) {
        return repository.addBalance(addr, value, assetID);
    }

    @Override
    public Set<byte[]> getAccountsKeys() {
        return repository.getAccountsKeys();
    }

    @Override
    public Repository startTracking() {
        return this;
    }

    @Override
    public void commit() {

    }

    @Override
    public void rollback() {

    }

    //
//    @Override
//    public void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash) {
//        repository.dumpState(block, gasUsed, txNumber, txHash);
//    }
//
//    @Override
//    public Repository startTracking() {
//        return repository.startTracking();
//    }
//
//    @Override
//    public void flush() {
//        repository.flush();
//    }
//
//    @Override
//    public void flushNoReconnect() {
//        throw new UnsupportedOperationException();
//    }
//
//
//    @Override
//    public void commit() {
//        repository.commit();
//    }
//
//    @Override
//    public void rollback() {
//        repository.rollback();
//    }
//
//    @Override
//    public void syncToRoot(byte[] root) {
//        repository.syncToRoot(root);
//    }
//
//    @Override
//    public boolean isClosed() {
//        return repository.isClosed();
//    }
//
//    @Override
//    public void close() {
//        repository.close();
//    }
//
//    @Override
//    public void reset() {
//        repository.reset();
//    }
//
////    @Override
////    public void updateBatch(HashMap<ByteArrayWrapper, Account> accountStates, HashMap<ByteArrayWrapper, ContractDetails> contractDetails) {
////        for (ByteArrayWrapper address : contractDetails.keySet()) {
////            if (!canListenTrace(address)) return;
////
////            ContractDetails details = contractDetails.get(address);
////            if (details.isDeleted()) {
////                programListener.onStorageClear();
////            } else if (details.isDirty()) {
////                for (Map.Entry<DataWord, DataWord> entry : details.getStorage().entrySet()) {
////                    programListener.onStoragePut(entry.getKey(), entry.getValue());
////                }
////            }
////        }
////        repository.updateBatch(accountStates, contractDetails);
////    }
//
//    @Override
//    public byte[] getRoot() {
//        return repository.getRoot();
//    }
//
//    @Override
//    public void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, Account> cacheAccounts, HashMap<ByteArrayWrapper, ContractDetails> cacheDetails) {
//        repository.loadAccount(addr, cacheAccounts, cacheDetails);
//    }
//
//    @Override
//    public Repository getSnapshotTo(byte[] root) {
//        throw new UnsupportedOperationException();
//    }
//
    @Override
    public Repository clone() {
        return new Storage(repository, address);
    }
//
//    @Override
//    public int getStorageSize(byte[] addr) {
//        return repository.getStorageSize(addr);
//    }
//
//    @Override
//    public Set<DataWord> getStorageKeys(byte[] addr) {
//        return repository.getStorageKeys(addr);
//    }
//
//    @Override
//    public Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys) {
//        return repository.getStorage(addr, keys);
//    }
}
