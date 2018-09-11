package com.uchain.core.datastore;

public class DataStoreConstant {
	public static final byte[] HeaderPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.BlockHeader) };

	public static final byte[] TxPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Transaction) };

	public static final byte[] AccountPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Account) };

	public static final byte[] HeightToIdIndexPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.BlockHeightToId) };

	public static final byte[] BlockIdToTxIdIndexPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.BlockIdToTxId) };

	public static final byte[] UTXOIndexPrefix = new byte[] {(byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.UTXO)};

	public static final byte[] NameToAccountIndexPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.NameToAccount) };

	public static final byte[] HeadBlockStatePrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) StateType.getStateType(StateType.HeadBlock) };
	
	public static final byte[] ProducerStatePrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) StateType.getStateType(StateType.Producer) };

}
