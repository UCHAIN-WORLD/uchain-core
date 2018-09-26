package com.uchain.core;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: AssetType
 *
 * @Author: bridge.bu@chinapex.com: 2018/9/20 15:58
 *
 * @Version: 1.0
 * *************************************************************/

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
