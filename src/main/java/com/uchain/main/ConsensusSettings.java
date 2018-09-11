package com.uchain.main;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsensusSettings {
	private List<Witness> witnessList;

	private int produceInterval;

	private int acceptableTimeError;

	private int producerRepetitions;

	public ConsensusSettings(List<Witness> witnessList, int produceInterval, int acceptableTimeError,
			int producerRepetitions) {
		this.witnessList = witnessList;
		this.produceInterval = produceInterval;
		this.acceptableTimeError = acceptableTimeError;
		this.producerRepetitions = producerRepetitions;
	}
	
	
}
