package com.uchain.core.transaction;


import java.util.TreeSet;

public class TransactionSorted extends TreeSet<Transaction> {
    public TransactionSorted() {
        super((tx1, tx2) -> {
            long nonceDiff = tx1.getNonce() - tx2.getNonce();
            if (nonceDiff != 0) {
                return nonceDiff > 0 ? 1 : -1;
            }
            return tx1.getId().compareTo(tx2.getId());
        });
    }
}
