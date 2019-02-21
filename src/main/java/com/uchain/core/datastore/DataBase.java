package com.uchain.core.datastore;

import com.uchain.core.Account;
import com.uchain.core.Code;
import com.uchain.core.Contract;
import com.uchain.core.datastore.keyvalue.*;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.main.DataBaseSettings;
import com.uchain.storage.Batch;
import com.uchain.storage.LevelDbStorage;
import com.uchain.storage.Storage;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Getter
@Setter
public class DataBase {

    private static final Logger log = LoggerFactory.getLogger(DataBase.class);
    private DataBaseSettings settings;
    private LevelDbStorage db;
    private Tracking tracking;

    private AccountStore accountStore;
    private ContractStorage contractStorage;
    private CodeStorage codeStorage;
    private NameToAccountStore nameToAccountStore;

    public DataBase(DataBaseSettings settings) {
        this.settings = settings;
        LevelDbStorage db = LevelDbStorage.open(settings.getDir());
        Tracking tracking = new TrackingRoot(db);
        this.db = db;
        this.tracking = tracking;
        this.accountStore = new AccountStore(tracking, settings.getCacheSize(), DataStoreConstant.AccountPrefix,
                new UInt160Key(), new AccountValue());
        this.contractStorage = new ContractStorage(tracking, settings.getCacheSize(), DataStoreConstant.ContractPrefix,
                new ByteKey(), new ContractValue());
        this.codeStorage = new CodeStorage(tracking, settings.getCacheSize(), DataStoreConstant.CodePrefix,
                new ByteKey(), new CodeValue());
        this.nameToAccountStore = new NameToAccountStore(db, settings.getCacheSize(),
                DataStoreConstant.NameToAccountIndexPrefix, new StringKey(), new UInt160Key());
    }

    public DataBase(DataBaseSettings settings, Tracking tracking, LevelDbStorage db){
        this.settings = settings;
        this.db = db;
        this.tracking = tracking;
        this.accountStore = new AccountStore(tracking, settings.getCacheSize(), DataStoreConstant.AccountPrefix,
                new UInt160Key(), new AccountValue());
        this.contractStorage = new ContractStorage(tracking, settings.getCacheSize(), DataStoreConstant.ContractPrefix,
                new ByteKey(), new ContractValue());
        this.codeStorage = new CodeStorage(tracking, settings.getCacheSize(), DataStoreConstant.CodePrefix,
                new ByteKey(), new CodeValue());
        this.nameToAccountStore = new NameToAccountStore(db, settings.getCacheSize(),
                DataStoreConstant.NameToAccountIndexPrefix, new StringKey(), new UInt160Key());
    }

    public Boolean nameExists(String name) {
        return nameToAccountStore.contains(name);
    }

    public Boolean registerExists(UInt160 register) {
        return accountStore.contains(register);
    }

    public Account getAccount(UInt160 address) {
        return accountStore.get(address);
    }

    public Boolean setAccount(UInt160 fromUInt160, Account fromAccount, UInt160 toUInt160, Account toAccount) {
        try {
            Batch batch = db.batchWrite();
            accountStore.set(fromUInt160, fromAccount, batch);

            accountStore.set(toUInt160, toAccount, batch);

            return db.applyBatch(batch);
        } catch (Exception e) {
            log.error("SetAccount Failed!", e);
            return false;
        }
    }

    public Boolean updateAccount(UInt160 int160, Account account) {
        try {
            Batch batch = db.batchWrite();
            accountStore.set(int160, account, batch);
            return db.applyBatch(batch);
        } catch (Exception e) {
            log.error("insert account Failed!", e);
            return false;
        }
    }

    public Contract getContract(UInt160 address) {
        return contractStorage.get(address.getData());
    }

    public Contract get(byte[] key) {
        byte[] value = contractStorage.getDb().get(key);
        if (value != null) {
            return contractStorage.getValConverter().fromBytes(value);
        } else {
            return null;
        }
    }

    public boolean createContract(UInt160 address, Contract contract) {
        try {
            Batch batch = db.batchWrite();
            contractStorage.set(address.getData(), contract, batch);
            return db.applyBatch(batch);
        } catch (Exception e) {
            log.error("insert account Failed!", e);
            return false;
        }
    }

    public boolean updateContract(UInt160 address, Contract contract) {
        try {
            Batch batch = db.batchWrite();
            contractStorage.set(address.getData(), contract, batch);
            return db.applyBatch(batch);
        } catch (Exception e) {
            log.error("insert account Failed!", e);
            return false;
        }
    }

    public boolean createCode(byte[] id, Code code) {
        try {
            Batch batch = db.batchWrite();
            codeStorage.set(id, code, batch);
            return db.applyBatch(batch);
        } catch (Exception e) {
            log.error("insert account Failed!", e);
            return false;
        }
    }

    public Code getCode(byte[] id) {
        return codeStorage.get(id);
    }

    public void delete(UInt160 key) {
        try {

            Batch batch = db.batchWrite();
            accountStore.delete(key, batch);
        } catch (Exception e) {
            log.error("delete account failed ", e);
        }
    }

    public Map<UInt256, Fixed8> getBalance(UInt160 address) {
        Account account = accountStore.get(address);
        return account.getBalances();
    }

    public void startSession() {
        db.newSession();
    }

    public void rollBack() {
        db.rollBack();
    }

    public void commit(int revision) {
        db.commit(revision);
    }

    public void commit() {
        db.commit();
    }

    public void close() {
        db.close();
    }

    public Integer revision() {
        return db.revision();
    }
}
