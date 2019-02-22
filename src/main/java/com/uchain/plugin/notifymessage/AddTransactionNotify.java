package com.uchain.plugin.notifymessage;

import com.uchain.core.transaction.Transaction;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddTransactionNotify implements NotifyMessage {

    private Transaction transaction;

    public AddTransactionNotify(Transaction transaction){
        this.transaction=transaction;
    }

}
