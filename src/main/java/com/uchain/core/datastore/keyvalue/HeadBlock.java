package com.uchain.core.datastore.keyvalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.core.BlockHeader;
import com.uchain.crypto.UInt256;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadBlock implements Serializable{
	private int height;
	private UInt256 id;
	public HeadBlock(int height, UInt256 id) {
		this.height = height;
		this.id = id;
	}

	public static HeadBlock deserialize(DataInputStream is) {
		int height = 0;
		try {
			height = is.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		HeadBlock headBlock = new HeadBlock(height, UInt256.deserialize(is));
		return headBlock;
	}

	public static ArrayList<UInt256> readSeq(DataInputStream is) throws IOException {
		int size = is.readInt();
		ArrayList<UInt256> uInt256 = new ArrayList<UInt256>(size);
		for(int i = 0; i < size; i++){
			uInt256.add(UInt256.deserialize(is));
		}
		return uInt256;
	}

	@Override
	public void serialize(DataOutputStream os) {
		try {
			os.writeInt(height);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Serializabler.write(os, id);	
	}
	
	public static HeadBlock fromHeader(BlockHeader header) {
		return new HeadBlock(header.getIndex(), header.id());
	}
}
