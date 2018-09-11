package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;

import lombok.val;

public class TransactionValue implements Converter<Transaction>{

	@Override
	public byte[] toBytes(Transaction key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public Transaction fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return Transaction.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
