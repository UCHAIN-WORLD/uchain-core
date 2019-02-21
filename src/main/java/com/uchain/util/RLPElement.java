package com.uchain.util;

import java.io.Serializable;

public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
