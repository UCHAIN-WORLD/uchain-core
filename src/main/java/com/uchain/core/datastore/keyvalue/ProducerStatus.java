package com.uchain.core.datastore.keyvalue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.uchain.common.Serializable;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ProducerStatus implements Serializable{
	private Long distance;

	public ProducerStatus(Long distance) {
		this.distance = distance;
	}

	public static ProducerStatus deserialize(DataInputStream is) {
		ProducerStatus producerStatus = null;
		try {
			producerStatus = new ProducerStatus(is.readLong());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return producerStatus;
	}

	@Override
	public void serialize(DataOutputStream os) {
		try {
			os.writeLong(distance);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
