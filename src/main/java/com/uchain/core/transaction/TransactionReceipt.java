package com.uchain.core.transaction;

import com.uchain.common.Serializabler;
import com.uchain.core.Identifier;
import com.uchain.cryptohash.PublicKeyHash;
import com.uchain.cryptohash.UInt256;
import com.uchain.cryptohash.UIntBase;
import com.uchain.util.ByteUtil;
import com.uchain.util.rlp.RLP;
import com.uchain.util.rlp.RLPElement;
import com.uchain.util.rlp.RLPItem;
import com.uchain.util.rlp.RLPList;
import com.uchain.uvm.LogInfo;
import org.spongycastle.util.BigIntegers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.uchain.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static com.uchain.util.ByteUtil.toHexString;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

public class TransactionReceipt implements Identifier<UInt256> {

    private Transaction transaction;

    private byte[] postTxState = EMPTY_BYTE_ARRAY;
    private byte[] cumulativeGas = EMPTY_BYTE_ARRAY;
//    private Bloom bloomFilter = new Bloom();
    private List<LogInfo> logInfoList = new ArrayList<>();

    private byte[] executionResult = EMPTY_BYTE_ARRAY;
    private String error = "";

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    private byte[] gasUsed = EMPTY_BYTE_ARRAY;
    private byte[] blockHash = EMPTY_BYTE_ARRAY;
    private int blockHeight = -1;
    private byte[] contractAddress = EMPTY_BYTE_ARRAY;
    private int txIndex = -1;


    public TransactionReceipt() {
    }

    public TransactionReceipt(Transaction tx) {
        this.transaction = tx;
    }

    public TransactionReceipt(byte[] rlp) {

        RLPList params = RLP.decode2(rlp);
        RLPList receipt = (RLPList) params.get(0);

        RLPItem postTxStateRLP = (RLPItem) receipt.get(0);
        RLPItem cumulativeGasRLP = (RLPItem) receipt.get(1);
//        RLPItem bloomRLP = (RLPItem) receipt.get(2);
        RLPList logs = (RLPList) receipt.get(2);
        RLPItem blockHashRLP = (RLPItem) receipt.get(3);
        RLPItem blockHeightRLP = (RLPItem) receipt.get(4);
        RLPItem contractAddressRLP = (RLPItem) receipt.get(5);
        RLPItem txIndexRLP = (RLPItem) receipt.get(6);
        RLPItem gasUsedRLP = (RLPItem) receipt.get(7);
        RLPItem result = (RLPItem) receipt.get(8);

        postTxState = nullToEmpty(postTxStateRLP.getRLPData());
        cumulativeGas = cumulativeGasRLP.getRLPData();
//        bloomFilter = new Bloom(bloomRLP.getRLPData());
        gasUsed = gasUsedRLP.getRLPData();
        executionResult = (executionResult = result.getRLPData()) == null ? EMPTY_BYTE_ARRAY : executionResult;
        blockHash = blockHashRLP.getRLPData();
        blockHeight = ByteUtil.byteArrayToInt(blockHeightRLP.getRLPData());
        contractAddress = contractAddressRLP.getRLPData();
        txIndex = ByteUtil.byteArrayToInt(txIndexRLP.getRLPData());
        if (receipt.size() > 6) {
            byte[] errBytes = receipt.get(6).getRLPData();
            error = errBytes != null ? new String(errBytes, StandardCharsets.UTF_8) : "";
        }

        for (RLPElement log : logs) {
            LogInfo logInfo = new LogInfo(log.getRLPData());
            logInfoList.add(logInfo);
        }

        rlpEncoded = rlp;
    }


    public TransactionReceipt(byte[] postTxState, byte[] cumulativeGas,
                              /*Bloom bloomFilter,*/ List<LogInfo> logInfoList) {
        this.postTxState = postTxState;
        this.cumulativeGas = cumulativeGas;
//        this.bloomFilter = bloomFilter;
        this.logInfoList = logInfoList;
    }

    public TransactionReceipt(final RLPList rlpList) {
        if (rlpList == null || rlpList.size() != 4)
            throw new RuntimeException("Should provide RLPList with postTxState, cumulativeGas, bloomFilter, logInfoList");

        this.postTxState = rlpList.get(0).getRLPData();
        this.cumulativeGas = rlpList.get(1).getRLPData();
//        this.bloomFilter = new Bloom(rlpList.get(2).getRLPData());

        List<LogInfo> logInfos = new ArrayList<>();
        for (RLPElement logInfoEl: (RLPList) rlpList.get(3)) {
            LogInfo logInfo = new LogInfo(logInfoEl.getRLPData());
            logInfos.add(logInfo);
        }
        this.logInfoList = logInfos;
    }

    public byte[] getPostTxState() {
        return postTxState;
    }

    public byte[] getCumulativeGas() {
        return cumulativeGas;
    }

    public byte[] getGasUsed() {
        return gasUsed;
    }

    public byte[] getExecutionResult() {
        return executionResult;
    }

    public long getCumulativeGasLong() {
        return new BigInteger(1, cumulativeGas).longValue();
    }


//    public Bloom getBloomFilter() {
//        return bloomFilter;
//    }

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public boolean isValid() {
        return ByteUtil.byteArrayToLong(gasUsed) > 0;
    }

    public boolean isSuccessful() {
        return error.isEmpty();
    }

    public String getError() {
        return error;
    }

    public byte[] getReceiptTrieEncoded() {
        return getEncoded(true);
    }

    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            rlpEncoded = getEncoded(false);
        }

        return rlpEncoded;
    }

    public byte[] getEncoded(boolean receiptTrie) {

        byte[] postTxStateRLP = RLP.encodeElement(this.postTxState);
        byte[] cumulativeGasRLP = RLP.encodeElement(this.cumulativeGas);
//        byte[] bloomRLP = EMPTY_BYTE_ARRAY;//RLP.encodeElement(this.bloomFilter.data);

        final byte[] logInfoListRLP;
        if (logInfoList != null) {
            byte[][] logInfoListE = new byte[logInfoList.size()][];

            int i = 0;
            for (LogInfo logInfo : logInfoList) {
                logInfoListE[i] = logInfo.getEncoded();
                ++i;
            }
            logInfoListRLP = RLP.encodeList(logInfoListE);
        } else {
            logInfoListRLP = RLP.encodeList();
        }
        byte[] blockHash = RLP.encodeElement(this.blockHash);
        byte[] blockHeight = RLP.encodeInt(this.blockHeight);
        byte[] contractAddress = RLP.encodeElement(this.contractAddress);
        byte[] txIndex = RLP.encodeInt(this.txIndex);
        return receiptTrie ?
                RLP.encodeList(postTxStateRLP, cumulativeGasRLP, logInfoListRLP
                ,blockHash,blockHeight,contractAddress,txIndex):
                RLP.encodeList(postTxStateRLP, cumulativeGasRLP, logInfoListRLP
                        ,blockHash,blockHeight,contractAddress,txIndex,
                        RLP.encodeElement(gasUsed), RLP.encodeElement(executionResult),
                        RLP.encodeElement(error.getBytes(StandardCharsets.UTF_8)));

    }

    public void setPostTxState(byte[] postTxState) {
        this.postTxState = postTxState;
        rlpEncoded = null;
    }

    public void setTxStatus(boolean success) {
        this.postTxState = success ? new byte[]{1} : new byte[0];
        rlpEncoded = null;
    }

    public boolean hasTxStatus() {
        return postTxState != null && postTxState.length <= 1;
    }

    public boolean isTxStatusOK() {
        return executionResult != null && contractAddress != null && error.isEmpty();
    }

    public void setCumulativeGas(long cumulativeGas) {
        this.cumulativeGas = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(cumulativeGas));
        rlpEncoded = null;
    }

    public void setCumulativeGas(byte[] cumulativeGas) {
        this.cumulativeGas = cumulativeGas;
        rlpEncoded = null;
    }

    public void setGasUsed(byte[] gasUsed) {
        this.gasUsed = gasUsed;
        rlpEncoded = null;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(gasUsed));
        rlpEncoded = null;
    }

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
        rlpEncoded = null;
    }

    public void setError(String error) {
        this.error = error == null ? "" : error;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        if (logInfoList == null) return;
        this.logInfoList = logInfoList;

        for (LogInfo loginfo : logInfoList) {
//            bloomFilter.or(loginfo.getBloom());
        }
        rlpEncoded = null;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        if (transaction == null) throw new NullPointerException("Transaction is not initialized. Use TransactionInfo and BlockStore to setup Transaction instance");
        return transaction;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public int getTxIndex() {
        return txIndex;
    }

    public void setBlockHash(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public void setTxIndex(int txIndex) {
        this.txIndex = txIndex;
    }

    @Override
    public String toString() {

        return "TransactionReceipt : {" +
                "\n " + (hasTxStatus() ? ("txStatus=" + (isTxStatusOK() ? "OK" : "FAILED"))
                                        : ("postTxState=" + toHexString(postTxState))) +
                "\n  , txHash='0x" + (transaction != null?transaction.id().toString():null) +"'"+
                "\n  , cumulativeGas=" + toHexString(cumulativeGas) +
                "\n  , gasUsed=" + (toHexString(gasUsed).isEmpty()?null:toHexString(gasUsed)) +
                "\n  , error='" + error +"'"+
                "\n  , executionResult=" + (toHexString(executionResult).isEmpty()?null:toHexString(executionResult)) +
                "\n  , logs=" + logInfoList +
                "\n  , contractAddress='" + (toHexString(contractAddress).isEmpty()?null: PublicKeyHash.toAddress(contractAddress)) +"'"+
                '}';
    }

    @Override
    public UIntBase id() {
        return null;
    }

    @Override
    public void serialize(DataOutputStream os) {
        try {
            Serializabler.writeByteArray(os,getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public long estimateMemSize() {
//        return MemEstimator.estimateSize(this);
//    }

//    public static final MemSizeEstimator<TransactionReceipt> MemEstimator = receipt -> {
//        if (receipt == null) {
//            return 0;
//        }
//        long logSize = receipt.logInfoList.stream().mapToLong(LogInfo.MemEstimator::estimateSize).sum() + 16;
//        return (receipt.transaction == null ? 0 : Transaction.MemEstimator.estimateSize(receipt.transaction)) +
//                (receipt.postTxState == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.postTxState)) +
//                (receipt.cumulativeGas == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.cumulativeGas)) +
//                (receipt.gasUsed == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.gasUsed)) +
//                (receipt.executionResult == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.executionResult)) +
//                ByteArrayEstimator.estimateSize(receipt.rlpEncoded) +
//                Bloom.MEM_SIZE +
//                receipt.error.getBytes().length + 40 +
//                logSize;
//    };
}
