package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;
import com.uchain.core.TransactionType;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Fixed8;
import com.uchain.crypto.PrivateKey;
import com.uchain.crypto.PublicKeyHash;
import com.uchain.crypto.UInt256Util;

import lombok.val;

public class TransactionTest {
	@Test
	public void testSerialize() throws IOException {
		PrivateKey privKey = PrivateKey
				.apply(new BinaryData("d39d51a8d40336b0c73af180308fe0e4ee357e45a59e8afeebf6895ddf78aa2f"));

		Transaction tx = new Transaction(TransactionType.Transfer,
				new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322"),
				PublicKeyHash.fromAddress("APGMmPKLYdtTNhiEkDGU6De8gNCk3bTsME9"), "bob", Fixed8.Ten, UInt256Util.Zero(),
				1L, new BinaryData("1234"), new BinaryData(new ArrayList<>()));

		tx.sign(privKey);
		assert (tx.verifySignature() == true);

		val bos = new ByteArrayOutputStream();
		val os = new DataOutputStream(bos);
		Serializabler.write(os, tx);
		val ba = bos.toByteArray();
		val bis = new ByteArrayInputStream(ba);
		val is = new DataInputStream(bis);
		val transactionDeserializer = Transaction.deserialize(is);
		
		assert(tx.getTxType() == transactionDeserializer.getTxType());
		assert(tx.getFrom().toString().equals(transactionDeserializer.getFrom().toString()));
		assert(tx.getToPubKeyHash().toString().equals(transactionDeserializer.getToPubKeyHash().toString()));
		assert(tx.getToName().equals(transactionDeserializer.getToName()));
		assert(tx.getAmount().eq(transactionDeserializer.getAmount()));
		assert(tx.getAssetId().toString().equals(transactionDeserializer.getAssetId().toString()));
		assert(tx.getNonce()==transactionDeserializer.getNonce());
		assert(tx.getData().toString().equals(transactionDeserializer.getData().toString()));
		assert(tx.getSignature().toString().equals(transactionDeserializer.getSignature().toString()));
		assert(tx.getVersion() == transactionDeserializer.getVersion());
		
		System.out.println("0377fb7a1fd741ccd78dbdedaa85f9009c01f1a687a1a17804d67b661d36f5c5cc".length());
		System.out.println("6e66e5712fbebd2e5517099027915b4ae9ab9fb47a2f73a830275f3e6da75c2a01".length());
	}
}
