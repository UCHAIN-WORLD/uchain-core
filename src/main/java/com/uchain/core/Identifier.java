package com.uchain.core;

import com.uchain.common.Serializable;
import com.uchain.cryptohash.UIntBase;

public interface Identifier<T extends UIntBase> extends Serializable {
	
	UIntBase id();
}
