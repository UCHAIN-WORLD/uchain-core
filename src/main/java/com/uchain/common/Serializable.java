package com.uchain.common;

import java.io.DataOutputStream;

public interface Serializable {
	void serialize(DataOutputStream os);
}
