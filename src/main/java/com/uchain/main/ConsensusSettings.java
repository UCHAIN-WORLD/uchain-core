package com.uchain.main;

import com.uchain.crypto.BinaryData;
import com.uchain.crypto.Crypto;
import com.uchain.crypto.CryptoUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class ConsensusSettings {
	private List<Witness> witnessList;

	private int produceInterval;

	private int acceptableTimeError;

	private int producerRepetitions;

	private int runtimeParasStopProcessTxTimeSlot;

	public ConsensusSettings(List<Witness> witnessList, int produceInterval, int acceptableTimeError,
                             int producerRepetitions, int runtimeParasStopProcessTxTimeSlot) {
		this.witnessList = witnessList;
		this.produceInterval = produceInterval;
		this.acceptableTimeError = acceptableTimeError;
		this.producerRepetitions = producerRepetitions;
		this.runtimeParasStopProcessTxTimeSlot = runtimeParasStopProcessTxTimeSlot;
	}

    public BinaryData fingerprint() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bs);
        try {
            os.writeInt(produceInterval);
            os.writeInt(acceptableTimeError);
            os.writeInt(producerRepetitions);
            witnessList.forEach(w->{
                try {
                    os.writeBytes(w.getName());
                    os.writeBytes(w.getPubkey().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CryptoUtil.array2binaryData(Crypto.hash256(bs.toByteArray()));
    }
	
}
