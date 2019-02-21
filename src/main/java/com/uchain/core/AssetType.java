package com.uchain.core;

import lombok.val;
import scala.Enumeration;

public class AssetType extends Enumeration {
   // public val Token = Value(0x01);
    public val id;

    protected val id(){
        if(id == null){
            return genId();
        }
        return id;
    }

    protected val genId(){
        return id;
    }

    public int getValue(AssetType.Value t){
        return t.id();
    }


}
