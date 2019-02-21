package com.uchain.common;

import com.uchain.vm.DataWord;

import java.io.DataOutputStream;

public interface Serializable{

	void serialize(DataOutputStream os);
}
