package com.uchain.network.message;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.crypto.UInt256;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetBlocksPayload implements Serializable{
	private List<UInt256> hashStart;
	private UInt256 hashStop;
	@Override
	public void serialize(DataOutputStream os) {
		Serializabler.writeSeq(os, hashStart);
		hashStop.serialize(os);
	}

	public static List<UInt256> readSeq(DataInputStream is) {
		List<UInt256> uInt256s = null;
		try {
			int size = is.readInt();
			uInt256s = new ArrayList<UInt256>(size);
			for(int i = 0; i < size; i++){
				uInt256s.add(UInt256.deserialize(is));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return uInt256s;
	}
	
	private static GetBlocksPayload deserialize(DataInputStream is) {
		List<UInt256> hashStartTemp = readSeq(is);
		UInt256 hashStopTemp = UInt256.deserialize(is);
		return new GetBlocksPayload(hashStartTemp, hashStopTemp);
	}
	public static GetBlocksPayload fromBytes(byte[] data) {
		ByteArrayInputStream bs = new ByteArrayInputStream(data);
		DataInputStream is = new DataInputStream(bs);
		return deserialize(is);
	}
}
