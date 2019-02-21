package com.uchain;
import com.google.common.collect.Maps;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.UInt256;
import lombok.val;
import org.junit.Test;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class AccountTest {

    @Test
    public void testSerialize() throws IOException {
        Map<UInt256, Fixed8> balances = Maps.newLinkedHashMap();
        for(int i = 1; i<= 10; i++){
            UInt256 key = UInt256.fromBytes(Crypto.hash256(("test"+i).getBytes("UTF-8")));
            Fixed8 value = new Fixed8(Long.parseLong(i+""));
            balances.put(key, value);
        }
        val a = new Account(false, "",balances, 0, 0x01,null);
        val bos = new ByteArrayOutputStream();
        val os = new DataOutputStream(bos);
        Serializabler.write(os, a);
        val ba = bos.toByteArray();
        val bis = new ByteArrayInputStream(ba);
        val is = new DataInputStream(bis);
        val accountDeserializer = Account.deserialize(is);

        assert(a.isActive() == accountDeserializer.isActive());
        assert(a.getName().equals(accountDeserializer.getName()));
        assert(a.getNextNonce().longValue() == accountDeserializer.getNextNonce().longValue());
        assert(a.getVersion() == accountDeserializer.getVersion());
        Iterator<Entry<UInt256, Fixed8>> iter1 = balances.entrySet().iterator();
        Iterator<Entry<UInt256, Fixed8>> iter2 = accountDeserializer.getBalances().entrySet().iterator();
        while(iter1.hasNext() && iter2.hasNext()){
            Map.Entry<UInt256, Fixed8> entry1 = (Entry<UInt256, Fixed8>) iter1.next();
            Map.Entry<UInt256, Fixed8> entry2 = (Entry<UInt256, Fixed8>) iter2.next();
            assert(entry1.getKey().toString().equals(entry2.getKey().toString()));
            assert(entry1.getValue().toString().equals(entry2.getValue().toString()));
        }
    }
}
