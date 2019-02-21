package com.uchain.uvm.repository;

import com.google.common.collect.Maps;
import com.sun.istack.internal.Nullable;
import com.uchain.core.Account;
import com.uchain.core.Code;
import com.uchain.core.Contract;
import com.uchain.core.consensus.ForkBase;
import com.uchain.core.datastore.BlockBase;
import com.uchain.core.datastore.DataBase;
import com.uchain.core.datastore.NodeKeyCompositor;
import com.uchain.core.datastore.Tracking;
import com.uchain.cryptohash.Fixed8;
import com.uchain.cryptohash.HashUtil;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.util.BIUtil;
import com.uchain.util.ByteUtil;
import com.uchain.util.FastByteComparisons;
import com.uchain.uvm.ContractDetails;
import com.uchain.uvm.DataWord;
import com.uchain.uvm.Repository;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.util.*;

@Getter
@Setter
public class RepositoryImpl implements Repository {

    private ForkBase forkBase;
    private DataBase dataBase;
    private BlockBase blockBase;

    public RepositoryImpl(ForkBase forkBase, DataBase dataBase, BlockBase blockBase) {
        this.forkBase = forkBase;
        this.dataBase = dataBase;
        this.blockBase = blockBase;
    }


    @Override
    public synchronized Account createAccount(UInt160 id) {
        Map<UInt256, Fixed8> frombalances = Maps.newHashMap();
        Account account = new Account(BigInteger.ZERO, frombalances, id);
        dataBase.updateAccount(id, account);
        return account;
    }

    @Override
    public synchronized boolean isExist(UInt160 id) {
        return getAccount(id) != null;
    }

    @Override
    public synchronized Account getAccount(UInt160 id) {
        return dataBase.getAccount(id);
    }

    synchronized Account getOrCreateAccount(UInt160 id) {
        Account ret = dataBase.getAccount(id);
        if (ret == null) {
            ret = createAccount(id);
        }
        return ret;
    }

    @Override
    public synchronized void delete(UInt160 id) {
        dataBase.delete(id);
        //storageCache.delete(addr);
    }

    @Override
    public synchronized Long increaseNonce(UInt160 id) {
        Account accountState = getOrCreateAccount(id);
        dataBase.updateAccount(id, accountState.withIncrementedNonce());
        return accountState.getNextNonce();
    }

    @Override
    public synchronized Long setNonce(UInt160 id, Long nonce) {
        Account accountState = getOrCreateAccount(id);
        dataBase.updateAccount(id, accountState.withNonce(nonce));
        return accountState.getNextNonce();
    }

    @Override
    public synchronized BigInteger getNonce(UInt160 id) {
        Account accountState = getAccount(id);
        return accountState == null ? BigInteger.ZERO :
                BigInteger.valueOf(accountState.getNextNonce());
    }

    @Override
    public synchronized ContractDetails getContractDetails(UInt160 addr) {
        return new ContractDetailsImpl(addr);
    }

    @Override
    public synchronized boolean hasContractDetails(UInt160 addr) {
        return getContractDetails(addr) != null;
    }

    @Override
    public synchronized void saveCode(UInt160 addr, byte[] code) {
        byte[] codeHash = HashUtil.sha3(code);
        dataBase.createCode(codeKey(codeHash, addr), new Code(code, codeKey(codeHash, addr)));
        if (dataBase.getContract(addr) == null) {
            dataBase.createContract(addr, new Contract(new HashMap<>(), codeHash, addr));
        } else {
            dataBase.createContract(addr, dataBase.getContract(addr).updateContract(codeHash));
        }
    }

    @Override
    public synchronized byte[] getCode(UInt160 addr) {
        byte[] codeHash = getCodeHash(addr);
        return codeHash == null || FastByteComparisons.equal(codeHash, HashUtil.EMPTY_DATA_HASH) ?
                ByteUtil.EMPTY_BYTE_ARRAY : dataBase.getCode(codeKey(codeHash, addr)).getCode();
    }

    private byte[] codeKey(byte[] codeHash, UInt160 addr) {
        return NodeKeyCompositor.compose(codeHash, addr.getData());
    }

    @Override
    public byte[] getCodeHash(UInt160 addr) {
        Contract contract = getContract(addr);
        return contract != null ? contract.getCode() : null;
    }

    public Contract getContract(UInt160 addr) {
        return dataBase.getContract(addr);
    }

    @Override
    public synchronized void addStorageRow(UInt160 addr, DataWord key, DataWord value) {
        getOrCreateAccount(addr);
        Contract contract = dataBase.getContract(addr);
        Map map = Maps.newHashMap();
        map.put(key, value);
        if (contract == null) {
            dataBase.createContract(addr, new Contract(map, new byte[0], addr));
        } else {
            contract.getContractMap().put(key, value.isZero() ? DataWord.ZERO : value);
            dataBase.updateContract(addr, contract);
        }
    }

    @Override
    public synchronized DataWord getStorageValue(UInt160 addr, DataWord key) {
        Account accountState = getAccount(addr);
        Contract contract = dataBase.getContract(addr);
        if (accountState != null) {
            if (contract == null) {
                dataBase.createContract(addr, new Contract(new HashMap<>(), new byte[0], addr));
            } else {
                DataWord result = contract.getContractMap().get(key);
                return result;
            }
        }
        return null;
    }

    @Override
    public synchronized BigInteger getBalance(UInt160 addr, UInt256 assetId) {
        Account accountState = getAccount(addr);
        return accountState == null ? BigInteger.ZERO : accountState.getBalance(assetId).getValue();
    }

    @Override
    public synchronized BigInteger addBalance(UInt160 addr, BigInteger value, UInt256 assetID) {
        Account accountState = getOrCreateAccount(addr);
        dataBase.updateAccount(addr, accountState.withBalanceIncrement(assetID, value));
        return accountState.getBalance(assetID).getValue();
    }

    public synchronized BigInteger addBalance(UInt160 addr, BigInteger value) {
        return addBalance(addr, value, UInt256.assetId);
    }

    @Override
    public synchronized void transfer(UInt160 fromAddr, UInt160 toAddr, BigInteger value) {
        Account fromAccount = getOrCreateAccount(fromAddr);
        Fixed8 fixed8 = new Fixed8(value);
        fromAccount.updateBalance(BIUtil.getAssetId(), Fixed8.Zero.mus(fixed8));
        dataBase.updateAccount(fromAddr, fromAccount);
        Account toAccount = getOrCreateAccount(toAddr);
        toAccount.updateBalance(BIUtil.getAssetId(), fixed8);
        dataBase.updateAccount(toAddr, toAccount);
    }
    @Override
    public RepositoryImpl startTracking() {
        DataBase dataBase = new DataBase(this.dataBase.getSettings(), new Tracking(this.dataBase.getDb()), this.dataBase.getDb());

        return new RepositoryImpl(forkBase, dataBase, blockBase);
        /*Source<byte[], Account> trackAccountStateCache = new WriteCache.BytesKey<>(accountStateCache,
                WriteCache.CacheType.SIMPLE);
        Source<byte[], byte[]> trackCodeCache = new WriteCache.BytesKey<>(codeCache, WriteCache.CacheType.SIMPLE);
        MultiCache<CachedSource<DataWord, DataWord>> trackStorageCache = new MultiCache(storageCache) {
            @Override
            protected CachedSource create(byte[] key, CachedSource srcCache) {
                return new WriteCache<>(srcCache, WriteCache.CacheType.SIMPLE);
            }
        };

        RepositoryImpl ret = new RepositoryImpl(trackAccountStateCache, trackCodeCache, trackStorageCache);
        ret.parent = this;
        return ret;*/
    }
//
//    @Override
//    public synchronized Repository getSnapshotTo(byte[] root) {
//        return parent.getSnapshotTo(root);
//    }

    @Override
    public synchronized void commit() {
//        Repository parentSync = parent == null ? this : parent;
//        synchronized (parentSync) {
//            storageCache.flush();
//            codeCache.flush();
//            accountStateCache.flush();
//        }
    }

    @Override
    public synchronized void rollback() {
        dataBase.rollBack();
    }

    //
//    @Override
//    public byte[] getRoot() {
//        throw new RuntimeException("Not supported");
//    }
//
//    public synchronized String getTrieDump() {
//        return dumpStateTrie();
//    }
//
//    public String dumpStateTrie() {
//        throw new RuntimeException("Not supported");
//    }
//
    @Override
    public Repository clone() {
        return this;
    }

    class ContractDetailsImpl implements ContractDetails {
        private UInt160 address;

        public ContractDetailsImpl(UInt160 address) {
            this.address = address;
        }

        @Override
        public void put(DataWord key, DataWord value) {
            RepositoryImpl.this.addStorageRow(address, key, value);
        }

        @Override
        public DataWord get(DataWord key) {
            return RepositoryImpl.this.getStorageValue(address, key);
        }

        @Override
        public byte[] getCode() {
            return RepositoryImpl.this.getCode(address);
        }

        @Override
        public byte[] getCode(byte[] codeHash) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setCode(byte[] code) {
            RepositoryImpl.this.saveCode(address, code);
        }

        @Override
        public byte[] getStorageHash() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void decode(byte[] rlpCode) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDirty(boolean dirty) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setDeleted(boolean deleted) {
            RepositoryImpl.this.delete(address);
        }

        @Override
        public boolean isDirty() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public boolean isDeleted() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getEncoded() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public int getStorageSize() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Set<DataWord> getStorageKeys() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage(@Nullable Collection<DataWord> keys) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public Map<DataWord, DataWord> getStorage() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setStorage(List<DataWord> storageKeys, List<DataWord> storageValues) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void setStorage(Map<DataWord, DataWord> storage) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public byte[] getAddress() {
            return address.getData();
        }

        @Override
        public void setAddress(byte[] address) {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails clone() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public void syncStorage() {
            throw new RuntimeException("Not supported");
        }

        @Override
        public ContractDetails getSnapshotTo(byte[] hash) {
            throw new RuntimeException("Not supported");
        }
    }


    @Override
    public Set<byte[]> getAccountsKeys() {
        throw new RuntimeException("Not supported");
    }
}
