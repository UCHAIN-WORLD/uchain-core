package com.uchain.vm;

import com.uchain.core.Account;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import java.math.BigInteger;
import java.util.Set;

public interface Repository{

    //Account createAccount(byte[] addr);

    Account createAccount(UInt160 id);

    Account getAccount(UInt160 id);

    void delete(UInt160 id);

    Long increaseNonce(UInt160 id);

    Long setNonce(UInt160 id, Long nonce);

    ContractDetails getContractDetails(UInt160 addr);

    boolean hasContractDetails(UInt160 addr);

    void saveCode(UInt160 addr, byte[] code);

    byte[] getCodeHash(UInt160 addr);

    void addStorageRow(UInt160 addr, DataWord key, DataWord value);

    BigInteger addBalance(UInt160 addr, BigInteger value, UInt256 assetID);

    Set<byte[]> getAccountsKeys();
//
//    void dumpState(Block block, long gasUsed, int txNumber, byte[] txHash);
//
    Repository startTracking();
//
//    void flush();
//    void flushNoReconnect();

//
    void commit();

    void rollback();
//
//    void syncToRoot(byte[] root);
//
//    boolean isClosed();
//
//    void close();
//
//    void reset();

//    BigInteger addBalance(byte[] addr, UInt256 assetID, BigInteger value);
//    void updateBatch(HashMap<ByteArrayWrapper, Account> Accounts,
//                     HashMap<ByteArrayWrapper, ContractDetails> contractDetailes);

//
//    byte[] getRoot();
//
//    void loadAccount(byte[] addr, HashMap<ByteArrayWrapper, Account> cacheAccounts,
//                     HashMap<ByteArrayWrapper, ContractDetails> cacheDetails);
//
//    Repository getSnapshotTo(byte[] root);

    Repository clone();

    boolean isExist(UInt160 id);

    BigInteger getBalance(UInt160 addr, UInt256 assetId);

    BigInteger getNonce(UInt160 id);

    byte[] getCode(UInt160 addr);

    DataWord getStorageValue(UInt160 addr, DataWord key);
//
//    int getStorageSize(byte[] addr);
//
//    Set<DataWord> getStorageKeys(byte[] addr);
//
//    Map<DataWord, DataWord> getStorage(byte[] addr, @Nullable Collection<DataWord> keys);
    void transfer(UInt160 fromAddr,UInt160 toAddr,BigInteger value);
}
