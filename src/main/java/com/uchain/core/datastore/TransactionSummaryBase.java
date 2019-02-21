package com.uchain.core.datastore;

import com.uchain.core.TransactionReceipt;
import com.uchain.core.datastore.keyvalue.TransactionSummaryValue;
import com.uchain.core.datastore.keyvalue.UInt256Key;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.TransactionSummarySettings;
import com.uchain.storage.Batch;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;

import java.util.List;

public class TransactionSummaryBase{
    LevelDbStorage db;
    TransactionSummarySettings settings;
    TransactionSummaryStore transactionSummaryStore;

    public TransactionSummaryBase(TransactionSummarySettings settings) {
        this.settings =  settings;
        this.db = ConnFacory.getInstance(settings.getDir());
        this.transactionSummaryStore = new TransactionSummaryStore(db,settings.getCacheSize(),
                DataStoreConstant.TxPrefix,new UInt256Key(),new TransactionSummaryValue());
    }

    public void add(TransactionReceipt transactionReceipt){
        Batch batch = db.batchWrite();
        transactionSummaryStore.set(transactionReceipt.getTransaction().id(),
                transactionReceipt,batch);
        db.applyBatch(batch);
    }

    public void add(List<TransactionReceipt> transactionReceipts){
        Batch batch = db.batchWrite();
        transactionReceipts.forEach(transactionReceipt->transactionSummaryStore.set(transactionReceipt.getTransaction().id(),
                        transactionReceipt,batch));
        db.applyBatch(batch);
    }

    public void close(){
        db.close();
    }

    public Boolean containTransaction(UInt256 id) {
        return transactionSummaryStore.contains(id);
    }

    public TransactionReceipt getTransactionReceipt(UInt256 id){
        return transactionSummaryStore.get(id);
    }

    public void startSession()  {
        db.newSession();
    }

    public void rollBack(){
        db.rollBack();
    }

    public void commit( int revision){
        db.commit(revision);
    }

    public void commit(){
        db.commit();
    }

    public Integer revision() {
        return db.revision();
    }

}
