package com.uchain.core.datastore;

public class DataStoreConstant {
	public static final byte[] HeaderPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.BlockHeader) };

	public static final byte[] TxPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Transaction) };

	public static final byte[] AccountPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Account) };

	public static final byte[] ContractPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Contract) };
	public static final byte[] CodePrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Code) };

	public static final byte[] HeightToIdIndexPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.BlockHeightToId) };

	public static final byte[] NameToAccountIndexPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) IndexType.getIndexType(IndexType.NameToAccount) };

	public static final byte[] HeadBlockStatePrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) StateType.getStateType(StateType.HeadBlock) };
	
	public static final byte[] BlockPrefix = new byte[]{(byte) StoreType.getStoreType(StoreType.Data),
			(byte) DataType.getDataType(DataType.Block)};

    public static final byte[] ForkItemPrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Data),
            (byte) DataType.getDataType(DataType.ForkItem) };

	public static final byte[] SwitchStatePrefix = new byte[] { (byte) StoreType.getStoreType(StoreType.Index),
			(byte) StateType.getStateType(StateType.SwitchState) };
}
