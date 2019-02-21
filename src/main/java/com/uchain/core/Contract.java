package com.uchain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
import com.uchain.util.Utils;
import com.uchain.vm.DataWord;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static com.uchain.common.Serializabler.readByteArray;

@Setter
@Getter
public class Contract implements Identifier<UInt160>{

    // contract code elements which need to be stored in storage.
    Map<DataWord, DataWord> contractMap;
    byte[] code;

    @JsonIgnore
    private UInt160 id;

    public Contract(Map<DataWord, DataWord> contractMap, byte[] code, UInt160 id) {
        this.contractMap = contractMap;
        this.code = code;
        this.id = id;
    }

    private void serializeExcludeId(DataOutputStream os) {
        try {
            Serializabler.writeMap(os, contractMap, true);
            Serializabler.writeByteArray(os, code);
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
        return UInt160.fromBytes(Crypto.hash256(bs.toByteArray()));
    }

    public static Contract deserialize(DataInputStream is) {
        try {
            Map<DataWord, DataWord> map= readMap(is);
            byte[] code = readByteArray(is);
            val id = UInt160.deserialize(is);
            return new Contract(map, code, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<DataWord, DataWord> readMap(DataInputStream is) throws IOException {
        Map<DataWord, DataWord> map = Maps.newLinkedHashMap();
        val size = Utils.readVarInt(is);
        for (int i = 1; i <= size; i++) {
            map.put(DataWord.deserialize(is), DataWord.deserialize(is));
        }
        return map;
    }

    public Contract updateContract(byte[] code){
        this.code = code;
        return this;
    }

    @Override
    public UIntBase id() {
        if (id == null) {
            id = genId();
        }
        return id;
    }

    @JsonProperty("id")
    public String idJson(){
        return id.toString();
    }

}
