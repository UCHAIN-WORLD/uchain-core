package com.uchain.core.datastore;

public enum StateType {
	HeadBlock(0x00), Producer(0x01), LatestConfirmed(0x02),SwitchState(0x03);
	private int value;

	private StateType(int value) {
		this.value = value;
	}

	public static int getStateType(StateType stateType) {
		for (StateType c : StateType.values()) {
			if (c.value == stateType.value) {
				return c.value;
			}
		}
		return 100;
	}
}
