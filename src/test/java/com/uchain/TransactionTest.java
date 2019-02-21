package com.uchain;

import com.uchain.common.Serializabler;
import com.uchain.core.Transaction;
import com.uchain.core.TransactionType;
import com.uchain.core.consensus.SortedMultiMap2;
import com.uchain.core.consensus.SortedMultiMap2Iterator;
import com.uchain.core.consensus.ThreeTuple;
import com.uchain.crypto.*;
import com.uchain.util.ByteUtil;
import lombok.val;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static com.uchain.util.TypeConverter.hexToByteArray;

public class TransactionTest {
	@Test
	public void testSerialize() throws IOException {
		PrivateKey privKey = PrivateKey
				.apply(new BinaryData("d39d51a8d40336b0c73af180308fe0e4ee357e45a59e8afeebf6895ddf78aa2f"));
		PrivateKey privKeyWrong = PrivateKey
				.apply(new BinaryData("d39d51a8d40336b0c73af280308fe0e4ee357e45a59e8afeebf6895ddf78aa2f"));

//		Transaction tx = new Transaction(TransactionType.Transfer,
//				PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322")),
//				PublicKeyHash.fromAddress("APGMmPKLYdtTNhiEkDGU6De8gNCk3bTsME9"), "bob", Fixed8.Ten, UInt256.Zero(),
//				1L, new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		UInt160 to = UInt160.fromBytes(Crypto.hash160(CryptoUtil.listTobyte(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322").getData())));
        PublicKey minerCoinFrom = PublicKey.apply(new BinaryData("0345ffbf8dc9d8ff15785e2c228ac48d98d29b834c2e98fb8cfe6e71474d7f6322"));
        PublicKey producer = PublicKey.apply(new BinaryData("03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682"));

        byte[] dataBytes = hexToByteArray("0x6060604052341561000f57600080fd5b60d08061001d6000396000f3006060604052600436106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634e70b1dc14604e57806360fe47b1146074575b600080fd5b3415605857600080fd5b605e6094565b6040518082815260200191505060405180910390f35b3415607e57600080fd5b60926004808035906020019091905050609a565b005b60005481565b80600081905550505600a165627a7a723058205d1c1fb6a1781cd5e9cf2945cd6fbe1b8ad0d7801e94e6712d8a9b3cf3281e610029");
        BinaryData data = new BinaryData(CryptoUtil.byteToList(dataBytes));
//        Transaction tx = new Transaction(TransactionType.Miner, minerCoinFrom,
//				to, "", Fixed8.Ten, UInt256.Zero(), 1L,
//                new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()));
		     val tx = new Transaction(TransactionType.Miner, minerCoinFrom.pubKeyHash(),
				to, "", Fixed8.Ten, UInt256.Zero(),  1L,
                     data, new BinaryData(new ArrayList<>()),0x01,null, ByteUtil.hexStringToBytes("0x10000000000"),ByteUtil.hexStringToBytes("0x1000000"));

		tx.sign(privKeyWrong);
		assert (tx.verifySignature() == false);
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
//		assert(tx.getFrom().getData().equals(transactionDeserializer.getFrom().getData()));
//		assert(tx.getToPubKeyHash().toString().equals(transactionDeserializer.getToPubKeyHash().toString()));
//		assert(tx.getToName().equals(transactionDeserializer.getToName()));
//		assert(tx.getAmount().eq(transactionDeserializer.getAmount()));
//		assert(tx.getAssetId().toString().equals(transactionDeserializer.getAssetId().toString()));
//		assert(tx.getNonce()==transactionDeserializer.getNonce());
//		assert(tx.getData().toString().equals(transactionDeserializer.getData().toString()));
//		assert(tx.getSignature().getData().equals(transactionDeserializer.getSignature().getData()));
//		assert(tx.getVersion() == transactionDeserializer.getVersion());
//		assert(Arrays.equals(tx.getGasLimit(),transactionDeserializer.getGasLimit()));
//		assert(Arrays.equals(tx.getGasLimit(),transactionDeserializer.getGasLimit()));


		SortedMultiMap2<Integer, Integer, UInt256> sortedMultiMap2 = new SortedMultiMap2<>(
				"asc", "reverse");
		UInt256 ss0 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss1 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss2 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss4 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss5 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));
		UInt256 ss6 = UInt256.fromBytes(Crypto.hash256(("test"+0).getBytes("UTF-8")));

		sortedMultiMap2.put(0, 0, ss0);
		sortedMultiMap2.remove(0, 0);
		sortedMultiMap2.put(0, 1, ss0);
		sortedMultiMap2.head();
		sortedMultiMap2.put(1, 0, ss1);
//		System.out.println("1111111");
		sortedMultiMap2.head();
		sortedMultiMap2.remove(1, 0);
		sortedMultiMap2.put(1, 1, ss1);
		//sortedMultiMap2.put(2, false, null)
		sortedMultiMap2.head();
		sortedMultiMap2.remove(0, 1);
		System.out.println(sortedMultiMap2.size());
//		sortedMultiMap2.remove(2, false);
//		sortedMultiMap2.put(2, true, ss2);
//		System.out.println(sortedMultiMap2.size());
//		System.out.println(sortedMultiMap2.get(2,true));
		SortedMultiMap2Iterator<Integer, Integer, UInt256> a = sortedMultiMap2.iterator();
		while (a.hasNext()){
			ThreeTuple<Integer, Integer, UInt256> b = a.next();
			System.out.println(b.first+"  "+b.second);
		}
	}

	@Test
	public void testLength() throws IOException{
		byte[] dataBytes = hexToByteArray("6060604052341561000f57600080fd5b6101f08061001e6000396000f300606060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632c04477914610067578063457094cc146100bc5780634e70b1dc146100d157806360fe47b1146100fa575b600080fd5b341561007257600080fd5b61007a61011d565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156100c757600080fd5b6100cf610125565b005b34156100dc57600080fd5b6100e461018f565b6040518082815260200191505060405180910390f35b341561010557600080fd5b61011b6004808035906020019091905050610195565b005b600033905090565b7f05c766d1c5ea6f40afc38cd8e27308c236c492fbcfa32b458d2755cf76ec1e216040518080602001828103825260048152602001807f666972650000000000000000000000000000000000000000000000000000000081525060200191505060405180910390a1565b60005481565b80600081905550612222600102611111604051808260010260001916815260200191505060405180910390a1505600a165627a7a723058208bc7328e45955dcaec20f986d7cd700926db2fd6cf6f2c4c44f14f997fc371870029");
		BinaryData data = new BinaryData(CryptoUtil.byteToList(dataBytes));
		val bos = new ByteArrayOutputStream();
		val os = new DataOutputStream(bos);
		Serializabler.writeByteArray(os, CryptoUtil.binaryData2array(data));
		val ba = bos.toByteArray();
		val bis = new ByteArrayInputStream(ba);
		val is = new DataInputStream(bis);
		val dataDeserializer = CryptoUtil.array2binaryData(Serializabler.readByteArray(is));
		System.out.println(dataDeserializer);
	}
}
