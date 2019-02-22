package com.uchain.core.transaction;

public enum TransactionType {
	Miner(0x00),Transfer(0x01),Fee(0x02),RegisterName(0x03),Genesis(0x04),Contract(0x05),Estimate_ContractConsumer(0x06);

    private int value;

    private TransactionType(int value){
        this.value = value;
    }

    public static TransactionType getTransactionTypeByValue(byte value) {
        for (TransactionType c : TransactionType.values()) {
            if ((byte)c.value == value) {
                return c;
            }
        }
        return null;
    }
    
    public static int getTransactionTypeByType(TransactionType transactionType) {
        for (TransactionType c : TransactionType.values()) {
            if (c.value == transactionType.value) {
                return c.value;
            }
        }
        return 100;
    }
    
    public static String getTransactionTypeStringByType(TransactionType transactionType) {
        for (TransactionType c : TransactionType.values()) {
            if (c.value == transactionType.value) {
                return c.toString();
            }
        }
        return "";
    }
    
    public static void main(String[] args) {
    	System.out.println(TransactionType.getTransactionTypeByType(TransactionType.RegisterName));
	}
}