package com.uchain.storage;

import lombok.Getter;
import lombok.Setter;
import scala.Array;

@Getter
@Setter
public class DeleteOperationItem implements BatchItem{
    private byte[] key;

    public DeleteOperationItem(byte[] key){
        this.key = key;
    }
}
