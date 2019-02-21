package com.uchain.core;

import com.uchain.cryptohash.Crypto;
import com.uchain.cryptohash.UInt256;
import com.uchain.cryptohash.UIntBase;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class Asset implements Identifier<UInt256>{
/*
    val assetType: AssetType.Value,
    val issuer: UInt160,
    val name: String,
    val amount: Fixed8,
    val available: Fixed8,
    val precision: Byte,
    val fee: Fixed8,
    val active: Boolean,
    val version: Int = 0x01,
    override protected var _id: UInt256 = null
*/
    public Asset(){

    }
    @Override
    public void serialize(DataOutputStream os) {

    }

    @Override
    public UIntBase id() {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializeExcludeId(os);
        return UInt256.fromBytes(Crypto.hash256(bs.toByteArray()));
    }

    private DataOutputStream serializeExcludeId(DataOutputStream os){
        /*
        os.writeInt(version);
        os.writeByte(assetType.toByte);
        os.write(issuer);
        os.writeString(name);
        os.write(amount);
        os.write(available);
        os.writeByte(precision)
        os.write(fee)
        os.writeBoolean(active)
        */
        return os;
    }
}
