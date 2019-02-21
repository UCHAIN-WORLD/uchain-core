package com.uchain.plugin.notify;

import com.uchain.core.Transaction;
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
