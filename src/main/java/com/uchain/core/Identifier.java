package com.uchain.core;

import com.uchain.common.Serializable;
import com.uchain.crypto.UIntBase;

public interface Identifier<T extends UIntBase> extends Serializable {
	
	UIntBase id();
}
