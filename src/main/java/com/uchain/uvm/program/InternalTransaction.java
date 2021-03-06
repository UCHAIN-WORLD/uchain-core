package com.uchain.uvm.program;

import com.uchain.core.transaction.Transaction;
import com.uchain.core.transaction.TransactionType;
import com.uchain.cryptohash.*;
import com.uchain.util.ByteUtil;
import com.uchain.util.rlp.RLP;
import com.uchain.util.rlp.RLPList;
import com.uchain.uvm.DataWord;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.apache.commons.lang3.ArrayUtils.*;
import static com.uchain.util.ByteUtil.toHexString;

public class InternalTransaction extends Transaction {

    private byte[] parentHash;
    private int deep;
    private int index;
    private boolean rejected = false;
    private String note;
    private byte[] sendAddress;
    private byte[] rlpEncoded;
    private boolean parsed;


    public InternalTransaction(byte[] rawData) {
        super(rawData);
    }

    public InternalTransaction(byte[] parentHash, int deep, int index, byte[] nonce, DataWord gasPrice, DataWord gasLimit,
                               byte[] sendAddress, byte[] receiveAddress, byte[] value, byte[] data, String note) {

        super(TransactionType.Contract,new UInt160(nullToEmpty(sendAddress)),
                new UInt160(receiveAddress),"",
                new Fixed8(ByteUtil.byteArrayToLong(nullToEmpty(value))),UInt256.assetId,
                ByteUtil.byteArrayToLong(nonce)
                ,CryptoUtil.array2binaryData(nullToEmpty(data))
                ,null,0x01,null,getData(gasPrice),getData(gasLimit));

        this.parentHash = parentHash;
        this.deep = deep;
        this.index = index;
        this.sendAddress = sendAddress;
        this.note = note;
//        this.parsed = true;
    }

    public InternalTransaction(TransactionType txType, UInt160 from, UInt160 toPubKeyHash, String toName, Fixed8 amount,
                               UInt256 assetId, Long nonce, BinaryData data, BinaryData signature, int version, UInt256 id,
                               byte[] gasPrice, byte[] gasLimit) {
        super(txType,from,toPubKeyHash,toName,amount,assetId,nonce,data,signature,version,id, gasPrice,gasLimit);
    }

    private static byte[] getData(DataWord gasPrice) {
        return (gasPrice == null) ? ByteUtil.EMPTY_BYTE_ARRAY : gasPrice.getData();
    }

    public void reject() {
        this.rejected = true;
    }


    public int getDeep() {
        rlpParse();
        return deep;
    }

    public int getIndex() {
        rlpParse();
        return index;
    }

    public boolean isRejected() {
        rlpParse();
        return rejected;
    }

    public String getNote() {
        rlpParse();
        return note;
    }

    @Override
    public byte[] getSender() {
        rlpParse();
        return from.getData();
    }

    public byte[] getParentHash() {
        rlpParse();
        return parentHash;
    }

    /*@Override
    public byte[] getEncoded() {
        if (rlpEncoded == null) {

            byte[] nonce = ByteUtil.longToBytes(getNonce());
            boolean isEmptyNonce = isEmpty(nonce) || (getLength(nonce) == 1 && nonce[0] == 0);

            this.rlpEncoded = RLP.encodeList(
                    RLP.encodeElement(isEmptyNonce ? null : nonce),
//                    RLP.encodeElement(this.parentHash),
                    RLP.encodeElement(getSender()),
                    RLP.encodeElement(getReceiveAddress()),
                    RLP.encodeElement(ByteUtil.bigIntegerToBytes(getValue())),
                    RLP.encodeElement(getGasPrice()),
                    RLP.encodeElement(getGasLimit()),
                    RLP.encodeElement(CryptoUtil.binaryData2array(getData())),
                    RLP.encodeString(this.note),
                    encodeInt(this.deep),
                    encodeInt(this.index),
                    encodeInt(this.rejected ? 1 : 0)
            );
        }

        return rlpEncoded;
    }*/

//    @Override
    public byte[] getEncodedRaw() {
        return getEncoded();
    }

//    @Override
    public synchronized void rlpParse() {
        if (parsed) return;
        RLPList decodedTxList = RLP.decode2(rlpEncoded);
        RLPList transaction = (RLPList) decodedTxList.get(0);

        setNonce(ByteUtil.byteArrayToLong(transaction.get(0).getRLPData()));
        this.parentHash = transaction.get(1).getRLPData();
        this.sendAddress = transaction.get(2).getRLPData();
        setToPubKeyHash(new UInt160(transaction.get(3).getRLPData()));
        setAmount(new Fixed8(ByteUtil.byteArrayToLong(nullToEmpty(transaction.get(4).getRLPData()))));
        setGasPrice(transaction.get(5).getRLPData());
        setGasLimit(transaction.get(6).getRLPData());
        setData(CryptoUtil.array2binaryData(transaction.get(7).getRLPData()));
        this.note = new String(transaction.get(8).getRLPData());
        this.deep = decodeInt(transaction.get(9).getRLPData());
        this.index = decodeInt(transaction.get(10).getRLPData());
        this.rejected = decodeInt(transaction.get(11).getRLPData()) == 1;

        this.parsed = true;
    }


    private static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    private static int bytesToInt(byte[] bytes) {
        return isEmpty(bytes) ? 0 : ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private static byte[] encodeInt(int value) {
        return RLP.encodeElement(intToBytes(value));
    }

    private static int decodeInt(byte[] encoded) {
        return bytesToInt(encoded);
    }

//    @Override
    public ECKey getKey() {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }

//    @Override
    public void sign(byte[] privKeyBytes) throws ECKey.MissingPrivateKeyException {
        throw new UnsupportedOperationException("Cannot sign internal transaction.");
    }

    @Override
    public String toString() {
        return "TransactionData [" +
                "  parentHash=" + toHexString(getParentHash()) +
                ", hash=" + this.id() +
                ", nonce=" + getNonce() +
                ", gasPrice=" + toHexString(getGasPrice()) +
                ", gas=" + toHexString(getGasLimit()) +
                ", sendAddress=" + toHexString(getSender()) +
                ", receiveAddress=" + toHexString(getReceiveAddress()) +
                ", value=" + getValue() +
                ", data=" + getData() +
                ", note=" + getNote() +
                ", deep=" + getDeep() +
                ", index=" + getIndex() +
                ", rejected=" + isRejected() +
                "]";
    }
}
