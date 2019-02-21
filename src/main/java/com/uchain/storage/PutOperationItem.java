package com.uchain.storage;

import lombok.Getter;
import lombok.Setter;
import scala.Array;

@Getter
@Setter
public class PutOperationItem implements BatchItem{

    private byte[] key;

    private byte[] value;

    public PutOperationItem(byte[] key, byte[] value){
        this.key = key;
        this.value = value;
    }

}
