package com.uchain.core.datastore;

import com.uchain.common.Serializabler;
import com.uchain.common.Serializable;
import com.uchain.util.Utils;
import lombok.val;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SessionItem implements Serializable {

    private Map<ByteArrayKey,byte[]> insert = new HashMap();
    private Map<ByteArrayKey,byte[]> update = new HashMap();
    private Map<ByteArrayKey,byte[]> delete = new HashMap();

    public Map<ByteArrayKey,byte[]> getInsert() {
        return insert;
    }

    public Map<ByteArrayKey,byte[]> getDelete() {
        return delete;
    }

    public Map<ByteArrayKey,byte[]> getUpdate() {
        return update;
    }

    public void serialize(DataOutputStream os) {
        try {
            writeBytes(os, insert);
            writeBytes(os, update);
            writeBytes(os, delete);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void fill(byte[] bytes){
        ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
        DataInputStream is = new DataInputStream(bs);
        try {
            Map<byte[],byte[]> map = Serializabler.readMap(is);
            map.forEach((key,value)->fillInsert(key,value));
            map.forEach((key,value)->fillUpdate(key,value));
            map.forEach((key,value)->fillDelete(key,value));
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Catch Exception!");
        }
    }

    public void clear(){
        insert.clear();
        update.clear();
        delete.clear();
    }

    public void fillInsert(byte[] k,byte[] v){
        insert.put(new ByteArrayKey(k),v);
    }

    public void fillUpdate(byte[] k,byte[] v){
        update.put(new ByteArrayKey(k),v);
    }

    public void fillDelete(byte[] k,byte[] v){
        delete.put(new ByteArrayKey(k),v);
    }

    private void writeBytes(DataOutputStream os,Map<ByteArrayKey,byte[]> dict) throws IOException {
        Utils.writeVarint(dict.size(),os);
        dict.forEach((k,v)->{
            try {
                Serializabler.writeByteArray(os, k.getBytes());
                Serializabler.writeByteArray(os, v);
            }catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    public byte[] toBytes() {
        val bs = new ByteArrayOutputStream();
        val os = new DataOutputStream(bs);
        this.serialize(os);
        return bs.toByteArray();
    }
}
