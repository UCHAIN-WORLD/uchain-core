package com.uchain.core.datastore.keyvalue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.uchain.common.Serializabler;
import com.uchain.core.Account;

import lombok.val;

public class AccountValue implements Converter<Account>{

	@Override
	public byte[] toBytes(Account key) {
		return Serializabler.toBytes(key);
	}

	@Override
	public Account fromBytes(byte[] bytes) {
		val bs = new ByteArrayInputStream(bytes);
		val is = new DataInputStream(bs);
		try {
			return Account.deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
