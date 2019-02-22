package com.uchain.util.rlp;

import java.io.Serializable;

public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
