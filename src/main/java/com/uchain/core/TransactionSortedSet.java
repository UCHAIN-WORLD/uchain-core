package com.uchain.core;


import java.util.TreeSet;

public class TransactionSortedSet extends TreeSet<Transaction> {
    public TransactionSortedSet() {
        super((tx1, tx2) -> {
            long nonceDiff = tx1.getNonce() - tx2.getNonce();
            if (nonceDiff != 0) {
                return nonceDiff > 0 ? 1 : -1;
            }
            return tx1.getId().compareTo(tx2.getId());
        });
    }
}
