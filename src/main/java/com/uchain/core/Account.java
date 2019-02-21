package com.uchain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.uchain.common.Serializabler;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import com.uchain.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

@Getter
@Setter
public class Account implements Identifier<UInt160> {
    private boolean active;  // 账户状态
    private String name;     //12字符串名字
    private Map<UInt256, Fixed8> balances; //UInt256资产类型，Fixed8资产余额
    private Long nextNonce; //  每发起一笔交易，增加1
    private int version;   //  预留
    @JsonIgnore
    private UInt160 id;   //
//    private byte[] codeHash; //合约代码的Hash值

    public Account(boolean active, String name, Map<UInt256, Fixed8> balances, long nextNonce, int version, UInt160 id) {
        this.name = name;
        this.active = active;
        this.balances = balances;
        this.nextNonce = nextNonce;
        this.version = version;
        this.id = id;
    }


    public Fixed8 getBalance(UInt256 assetID) {
        Iterator<UInt256> assetIds = balances.keySet().iterator();
        while (assetIds.hasNext()) {
            UInt256 id = assetIds.next();
            if (assetID.equals(id)) {
                return balances.get(id);
            }
        }
        return Fixed8.Zero;
    }

    public void updateBalance(UInt256 assetID, Fixed8 value) {
        Iterator<UInt256> assetIds = balances.keySet().iterator();
        while (assetIds.hasNext()) {
            UInt256 id = assetIds.next();
            if (assetID.equals(id)) {
                Fixed8 before = balances.get(id);
                Fixed8 after = before.add(value);
                balances.put(id, after);
                return;
            }
        }
        balances.put(assetID, value);
    }

    private void serializeExcludeId(DataOutputStream os) {
        Map<UInt256, Fixed8> map = Maps.newLinkedHashMap();
        try {
            os.writeInt(version);
            os.writeBoolean(active);
            Serializabler.writeString(os, name);
            balances.forEach((key, value) -> {
                map.put(key, value);
            });
            Serializabler.writeMap(os, map);
            os.writeLong(nextNonce);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(DataOutputStream os) {
        serializeExcludeId(os);
        Serializabler.write(os, id());
    }

    private UInt160 genId() {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        serializeExcludeId(os);
        return UInt160.fromBytes(Crypto.hash160(bs.toByteArray()));
    }

    public static Account deserialize(DataInputStream is) {
        try {
            val version = is.readInt();
            val active = is.readBoolean();
            val name = Serializabler.readString(is);
            Map<UInt256, Fixed8> balances = readMap(is);
            val nextNonce = is.readLong();
            val id = UInt160.deserialize(is);
            return new Account(active, name, balances, nextNonce, version, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<UInt256, Fixed8> readMap(DataInputStream is) throws IOException {
        Map<UInt256, Fixed8> map = Maps.newLinkedHashMap();
        val size = Utils.readVarInt(is);
        for (int i = 1; i <= size; i++) {
            map.put(UInt256.deserialize(is), Fixed8.deserialize(is));
        }
        return map;
    }

    @Override
    public UInt160 id() {
        if (id == null) {
            id = genId();
        }
        return id;
    }

    @JsonProperty("id")
    public String idJson() {
        return id.toAddressString();
    }

    public void IncNonce() {
        nextNonce += 1;
    }

    public Account withIncrementedNonce() {
        this.nextNonce = this.nextNonce + 1;
        return this;
    }

    public Account withNonce(Long nextNonce) {
        this.nextNonce = nextNonce;
        return this;
    }

    public boolean isEmpty() {
//        return FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH) &&
//                BigInteger.ZERO.equals(balance) &&
//                BigInteger.ZERO.equals(nonce);
        return false;
    }

    public Account(BigInteger nonce, Map<UInt256, Fixed8> frombalances, UInt160 id) {
        this(true, "", frombalances, nonce.longValue(), 0x01, id);

    }

//    public boolean isContractExist() {
//        return !FastByteComparisons.equal(codeHash, EMPTY_DATA_HASH);
//    }

    public Account withBalanceIncrement(UInt256 assetID, BigInteger value) {
        Fixed8 fixed8Value = new Fixed8(value);
        if (balances.get(assetID) == null) {
            balances.put(assetID, fixed8Value);
        } else {
            balances.put(assetID, balances.get(assetID).add(fixed8Value));
        }
        return this;
    }
}
