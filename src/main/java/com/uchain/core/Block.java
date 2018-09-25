package com.uchain.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uchain.common.Serializabler;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@Setter
@Getter
public class Block implements Identifier<UInt160>{
    private BlockHeader header;

    private List<Transaction> transactions;

    private Map<UInt256, Transaction> txMp = new HashMap<>();

    public Block (BlockHeader header, List<Transaction> transactions){
        this.header = header;
        this.transactions = transactions;
        transactions.forEach(transaction -> {
            txMp.put(transaction.id(), transaction);
        });
    }

    public UInt256 id(){
        return header.id();
    }

    public int height(){
        return header.getIndex();
    }
    
    public UInt256 prev() {
    	return header.getPrevBlock();
    }
    
    public long timeStamp(){
        return header.getTimeStamp();
    }

    public Transaction getTransaction(UInt256 id){
        return txMp.get(id);
    }

    public Transaction getTransaction(int index){
        if (index < 0 || index >= transactions.size()) return null;
        return transactions.get(index);
    }

    public ArrayList<UInt256> getTransactionIds(){
        val size = transactions.size();
        val ids = new ArrayList<UInt256>(size);
        transactions.forEach(transaction -> {
            ids.add(transaction.id());
        });
        return ids;
    }

    @Override
    public void serialize(DataOutputStream os){
        Serializabler.write(os, header);
        Serializabler.writeSeq(os, transactions);
    }

    public static Block deserialize(DataInputStream is) throws IOException{
        val header = BlockHeader.deserialize(is);
        val txs = readSeq(is);
        return new Block(header, txs);
    }
    
    public static List<Transaction> readSeq(DataInputStream is) throws IOException {
		int size = is.readInt();
		List<Transaction> transactions = new ArrayList<Transaction>();
		for(int i = 0; i < size; i++){
			transactions.add(Transaction.deserialize(is));
		}
		return transactions;
	}
    
    public static Block fromBytes(byte[] data){
        val bs = new ByteArrayInputStream(data);
        val is = new DataInputStream(bs);
        try {
			return deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }
}