package com.uchain.uvm;

import lombok.Getter;

import java.math.BigInteger;

@Getter
public class GasCost {

    private static volatile GasCost instance;

    protected GasCost(){}

    public static GasCost getInstance(){
        if(null == instance){
            synchronized (GasCost.class){
                if (null == instance){
                    instance = new GasCost();
                }
            }
        }

        return instance;
    }

    private final int STEP = 1;
    private final int SSTORE = 300;

    private final int ZEROSTEP = 0;
    private final int QUICKSTEP = 2;
    private final int FASTESTSTEP = 3;
    private final int FASTSTEP = 5;
    private final int MIDSTEP = 8;
    private final int SLOWSTEP = 10;
    private final int EXTSTEP = 20;

    private final int GENESISGASLIMIT = 1000000;
    private final int MINGASLIMIT = 125000;

    private final int BALANCE = 20;
    private final int SHA3 = 30;
    private final int SHA3_WORD = 6;
    private final int SLOAD = 50;
    private final int STOP = 0;
    private final int SUICIDE = 0;
    private final int CLEAR_SSTORE = 5000;
    private final int SET_SSTORE = 20000;
    private final int RESET_SSTORE = 5000;
    private final int REFUND_SSTORE = 15000;
    private final int REUSE_SSTORE = 200;
    private final int CREATE = 32000;

    private final int JUMPDEST = 1;
    private final int CREATE_DATA_BYTE = 5;
    private final int CALL = 40;
    private final int STIPEND_CALL = 2300;
    private final int VT_CALL = 9000;
    private final int NEW_ACCT_CALL = 25000;
    private final int MEMORY = 3;
    private final int SUICIDE_REFUND = 24000;
    private final int QUAD_COEFF_DIV = 512;
    private final int CREATE_DATA = 200;
    private final int TX_NO_ZERO_DATA = 68;
    private final int TX_ZERO_DATA = 4;
    private final int TRANSACTION = 21000;
    private final int TRANSACTION_CREATE_CONTRACT = 53000;
    private final int LOG_GAS = 375;
    private final int LOG_DATA_GAS = 8;
    private final int LOG_TOPIC_GAS = 375;
    private final int COPY_GAS = 3;
    private final int EXP_GAS = 10;
    private final int EXP_BYTE_GAS = 10;
    private final int IDENTITY = 15;
    private final int IDENTITY_WORD = 3;
    private final int RIPEMD160 = 600;
    private final int RIPEMD160_WORD = 120;
    private final int SHA256 = 60;
    private final int SHA256_WORD = 12;
    private final int EC_RECOVER = 3000;
    private final int EXT_CODE_SIZE = 20;
    private final int EXT_CODE_COPY = 20;
    private final int EXT_CODE_HASH = 400;
    private final int NEW_ACCT_SUICIDE = 0;
    private final BigInteger EXCHANGE_RATE10 = new BigInteger("10000000000");
    private final BigInteger EXCHANGE_RATE8 = new BigInteger("100000000");
    private final DataWord EXCHANGE_RATE_DIV = DataWord.of(10000000000L);
    private final String gasPrice = "0x8";
    private final String gasLimit = "0x7a1200";
}
