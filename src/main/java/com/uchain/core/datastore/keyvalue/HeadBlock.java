package com.uchain.core.datastore.keyvalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.uchain.crypto.UInt256;
import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
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

	public static HeadBlock deserialize(DataInputStream is) throws IOException {
		return new HeadBlock(is.readInt(), UInt256Util.deserialize(is));
	}

	public static ArrayList<UInt256> readSeq(DataInputStream is) throws IOException {
		int size = is.readInt();
		ArrayList<UInt256> uInt256 = new ArrayList<UInt256>(size);
		for(int i = 0; i < size; i++){
			uInt256.add(UInt256Util.deserialize(is));
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
}
