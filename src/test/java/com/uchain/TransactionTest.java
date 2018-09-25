package com.uchain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.uchain.crypto.*;
import org.junit.Test;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;
import com.uchain.core.TransactionType;

import lombok.val;

public class TransactionTest {
	@Test
	public void testSerialize() throws IOException {
		PrivateKey privKey = PrivateKey
				.apply(new BinaryData("d39d51a8d40336b0c73af180308fe0e4ee357e45a59e8afeebf6895ddf78aa2f"));

//		Transaction tx = new Transaction(TransactionType.Transfer,
//				PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322")),
//				PublicKeyHash.fromAddress("APGMmPKLYdtTNhiEkDGU6De8gNCk3bTsME9"), "bob", Fixed8.Ten, UInt256.Zero(),
//				1L, new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		UInt160 to = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData())));
        PublicKey minerCoinFrom = PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322"));
        PublicKey producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));
//        Transaction tx = new Transaction(TransactionType.Miner, minerCoinFrom,
//				to, "", Fixed8.Ten, UInt256.Zero(), 1L,
//                new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		     val tx = new Transaction(TransactionType.Miner, minerCoinFrom,
				to, "", Fixed8.Ten, UInt256.Zero(),  1L,
				new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()),0x01,null);

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
		assert(tx.getFrom().toBin().getData().equals(transactionDeserializer.getFrom().toBin().getData()));
		assert(tx.getToPubKeyHash().toString().equals(transactionDeserializer.getToPubKeyHash().toString()));
		assert(tx.getToName().equals(transactionDeserializer.getToName()));
		assert(tx.getAmount().eq(transactionDeserializer.getAmount()));
		assert(tx.getAssetId().toString().equals(transactionDeserializer.getAssetId().toString()));
		assert(tx.getNonce()==transactionDeserializer.getNonce());
		assert(tx.getData().getData().equals(transactionDeserializer.getData().getData()));
		assert(tx.getSignature().getData().equals(transactionDeserializer.getSignature().getData()));
		assert(tx.getVersion() == transactionDeserializer.getVersion());

	}
}
