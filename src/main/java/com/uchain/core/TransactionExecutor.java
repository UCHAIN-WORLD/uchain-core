package com.uchain.core;

import com.uchain.config.BlockchainConfig;
import com.uchain.config.SystemPropertiesConfig;

import com.uchain.core.datastore.BlockStore;
import com.uchain.cryptohash.CryptoUtil;

import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.SystemProperties;
import com.uchain.util.BIUtil;
import com.uchain.util.ByteArraySet;
import com.uchain.uvm.*;
import com.uchain.uvm.hook.VMHook;
import com.uchain.uvm.program.Program;
import com.uchain.uvm.program.ProgramResult;
import com.uchain.uvm.program.invoke.ProgramInvoke;
import com.uchain.uvm.program.invoke.ProgramInvokeFactory;
import com.uchain.uvm.program.invoke.ProgramInvokeFactoryImpl;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

import static com.uchain.util.BIUtil.toBI;
import static com.uchain.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static com.uchain.util.ByteUtil.toHexString;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.springframework.util.StringUtils.isEmpty;

public class TransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("execute");
    private static final Logger stateLogger = LoggerFactory.getLogger("state");

    private SystemProperties config;

    private Transaction tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private long gasUsedInTheBlock;
    private boolean readyToExecute = false;
    private String execError;

    private ProgramInvokeFactory programInvokeFactory;

    private UInt160 newContractAddress;

    private TransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;
    private BlockchainConfig blockchainConfig;
//    private final EthereumListener listener;

    private VM vm;
    private Program program;
//    private byte[] coinbase;    //矿工

    PrecompiledContracts.PrecompiledContract precompiledContract;

    BigInteger m_endGas = BigInteger.ZERO;
    long basicTxCost = 0;
    List<LogInfo> logs = null;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    boolean localCall = false;
    private final VMHook vmHook;
    private long stopProcessTxTime;

    public TransactionExecutor(Transaction tx, BlockChain chain, Repository track,long stopProcessTxTime) {
        this(tx, chain, track, 0,stopProcessTxTime);
    }

    public TransactionExecutor(Transaction tx, BlockChain chain, Repository track, long gasUsedInTheBlock,long stopProcessTxTime) {
        this.config = ((LevelDBBlockChain) chain).getSettings().getConfig();
        this.tx = tx;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = ((LevelDBBlockChain) chain).getBlockBase().getBlockStore();
//        this.listener = listener;
        this.gasUsedInTheBlock = gasUsedInTheBlock;
        this.m_endGas = toBI(tx.getGasLimit());

//        withCommonConfig(CommonConfig.getDefault());
        this.currentBlock = chain.getBlock(chain.getHeight());
        this.vmHook = VMHook.EMPTY;
        this.blockchainConfig = SystemPropertiesConfig.getBlockchainConfig().getConfigForBlock(currentBlock.getHeader().getIndex());
        this.programInvokeFactory = new ProgramInvokeFactoryImpl();
        this.stopProcessTxTime = stopProcessTxTime;
    }
//
//    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, /*BlockStore blockStore,*/
//                               ProgramInvokeFactory programInvokeFactory, Block currentBlock) {
//
//        this(tx, coinbase, track, /*blockStore,*/ programInvokeFactory, currentBlock, /*new EthereumListenerAdapter(),*/ 0, VMHook.EMPTY);
//    }

//    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, /*BlockStore blockStore,*/
//                               ProgramInvokeFactory programInvokeFactory, Block currentBlock,
//                               /*EthereumListener listener,*/ long gasUsedInTheBlock) {
//        this(tx, coinbase,track, /*blockStore,*/ programInvokeFactory, currentBlock, /*listener,*/ gasUsedInTheBlock, VMHook.EMPTY);
//    }
//
//    public TransactionExecutor(Transaction tx, byte[] coinbase, Repository track, /*BlockStore blockStore,*/
//                               ProgramInvokeFactory programInvokeFactory, Block currentBlock,
//                              /* EthereumListener listener,*/ long gasUsedInTheBlock, VMHook vmHook) {
//
//        this.tx = tx;
//        this.track = track;
//        this.cacheTrack = track.startTracking();
////        this.blockStore = blockStore;
//        this.programInvokeFactory = programInvokeFactory;
//        this.currentBlock = currentBlock;
////        this.listener = listener;
//        this.gasUsedInTheBlock = gasUsedInTheBlock;
////        this.m_endGas = toBI(tx.getGasLimit());
//        this.vmHook = isNull(vmHook) ? VMHook.EMPTY : vmHook;
//
////        withCommonConfig(CommonConfig.getDefault());
//    }
//
////    public TransactionExecutor withCommonConfig(CommonConfig commonConfig) {
////        this.commonConfig = commonConfig;
////        this.config = commonConfig.systemProperties();
////        this.blockchainConfig = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber());
////        return this;
////    }

    private void execError(String err) {
        logger.warn(err);
        execError = err;
    }

    public void init() {
        basicTxCost = tx.getTransactionCost();

        if (localCall) {
            readyToExecute = true;
            return;
        }

        BigInteger reqNonce = track.getNonce(UInt160.fromBytes(tx.getSender()));
        BigInteger txNonce = toBI(tx.getNonce());
        if (!reqNonce.equals(txNonce)) {
            execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));
            return;
        }
        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());

        if (txGasLimit.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {

            execError(String.format("Not enough gas for transaction execution: Require: %s Got: %s", basicTxCost, txGasLimit));

            return;
        }
        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
        BigInteger totalCost = tx.getValue().add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.fromPubKeyHash(), tx.getAssetId());

        if (senderBalance.compareTo(totalCost) < 0) {
            execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));
            return;
        }
        readyToExecute = true;
    }

    public void execute() {
        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(UInt160.fromBytes(tx.getSender()));

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);

            track.addBalance(UInt160.fromBytes(tx.getSender()), txGasCost.negate(), tx.getAssetId());

            if (logger.isInfoEnabled())
                logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private void call() {
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        precompiledContract = PrecompiledContracts.getContractForAddress(DataWord.of(targetAddress), config);

        if (precompiledContract != null) {
            long requiredGas = precompiledContract.getGasForData(CryptoUtil.binaryData2array(tx.getData()));

            BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));

            if (!localCall && m_endGas.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
                execError("Out of Gas calling precompiled contract 0x" + toHexString(targetAddress) +
                        ", required: " + spendingGas + ", left: " + m_endGas);
                m_endGas = BigInteger.ZERO;
                return;
            } else {

                m_endGas = m_endGas.subtract(spendingGas);

                // FIXME: save return for vm trace
                Pair<Boolean, byte[]> out = precompiledContract.execute(CryptoUtil.binaryData2array(tx.getData()));

                if (!out.getLeft()) {
                    execError("Error executing precompiled contract 0x" + toHexString(targetAddress));
                    m_endGas = BigInteger.ZERO;
                    return;
                }
            }

        } else {

            byte[] code = cacheTrack.getCode(new UInt160(targetAddress));
            if (ArrayUtils.isEmpty(code)) {
                m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                result.spendGas(basicTxCost);
            } else {
                ProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, track, newContractAddress, blockStore);

                this.vm = new VM(config, vmHook);
                this.program = new Program(cacheTrack.getCodeHash(new UInt160(targetAddress)), code, programInvoke, tx, config, vmHook,stopProcessTxTime);
            }
        }

        /*byte[] targetAddress = tx.getReceiveAddress();
        newContractAddress = UInt160.fromBytes(targetAddress);
        byte[] code = track.getCode(UInt160.fromBytes(targetAddress));
        if (ArrayUtils.isEmpty(code)) {
            m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock,
                    track, track, UInt160.fromBytes(targetAddress),blockStore);

            this.vm = new VM(config, vmHook);
            this.program = new Program(code, programInvoke, tx, config, vmHook);
        }*/
        BigInteger endowment = tx.getValue();
        if (endowment.longValue() != 0) {
            BIUtil.transfer(cacheTrack, tx.getSender(), targetAddress, endowment);
        }
        touchedAccounts.add(targetAddress);
    }

    private void create() {
        newContractAddress = tx.getContractAddress();

        Account existingAddr = track.getAccount(newContractAddress);
        if (existingAddr != null/* && existingAddr.isContractExist(*//*blockchainConfig*//*)*/) {
            execError("Trying to create a contract with existing contract address: 0x" + newContractAddress.toString());
            m_endGas = BigInteger.ZERO;
            return;
        }

        BigInteger oldBalance = track.getBalance(newContractAddress, tx.getAssetId());
        cacheTrack.createAccount(newContractAddress); //create a contract account
        cacheTrack.addBalance(newContractAddress, oldBalance, UInt256.assetId); //save the balance of contract account in case the account exists before

        cacheTrack.increaseNonce(newContractAddress);

        if (isEmpty(tx.getDatas())) {
            m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock,
                    cacheTrack, track, newContractAddress, blockStore);

            this.vm = new VM(config, vmHook);
            this.program = new Program(tx.getDatas(), programInvoke, tx, config, vmHook,stopProcessTxTime);
        }

//        BigInteger endowment = /*BigInteger.valueOf(result.getGasUsed()* CryptoUtil.array2binaryData(tx.getGasPrice()));*/
//                toBI(tx.getGasPrice()).multiply(BigInteger.valueOf(result.getGasUsed()));
//
//                System.out.println("endowment is ::::::::::::::::::"+endowment);
//


        cacheTrack.transfer(UInt160.fromBytes(tx.getSender()), newContractAddress, tx.getValue());
        touchedAccounts.add(newContractAddress.getData());
    }

    public void go() {
        if (!readyToExecute) return;

        try {
            if (vm != null) {
                // Charge basic cost of the transaction
                program.spendGas(basicTxCost, "TRANSACTION COST");

                if (config.isPlayVM())
                    vm.play(program);

                result = program.getResult();
                m_endGas = m_endGas.subtract(toBI(program.getResult().getGasUsed()));

                if (tx.isContractCreation() && !result.isRevert()) {
                    int returnDataGasValue = getLength(program.getResult().getHReturn()) *
                            blockchainConfig.getGasCost().getCREATE_DATA();
                    if (m_endGas.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                        if (!blockchainConfig.getConstants().createEmptyContractOnOOG()) {
                            program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("No gas to return just created contract",
                                    returnDataGasValue, program));
                            result = program.getResult();
                        }
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else if (getLength(result.getHReturn()) > blockchainConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                        // contract size is too large, can not be executed successfully
                        program.setRuntimeFailure(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                                returnDataGasValue, program));
                        result = program.getResult();
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else {
                        // create contract successfully
                        m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                        cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());//Contract code is stored in database
                    }
                }

                //未花费手续费返还 在 finalization
//                track.addBalance(tx.fromPubKeyHash(), m_endGas.multiply(toBI(tx.getGasPrice())), tx.getAssetId());

//                String err = config.getBlockchainConfig().getConfigForBlock(currentBlock.getNumber()).
//                        validateTransactionChanges(blockStore, currentBlock, tx, null);
//                if (err != null) {
//                    program.setRuntimeFailure(new RuntimeException("Transaction changes validation failed: " + err));
//                }

                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        throw result.getException();
                    } else {
                        execError("REVERT opcode executed");
                    }
                } else {
                    touchedAccounts.addAll(result.getTouchedAccounts());
                    cacheTrack.commit();
                }

            } else {
//                cacheTrack.commit();
                cacheTrack.commit();
            }

        } catch (Throwable e) {
            rollback();
            m_endGas = BigInteger.ZERO;
            execError(e.getMessage());
        }
    }

    private void rollback() {
        cacheTrack.rollback();

        touchedAccounts.remove(
                tx.isContractCreation() ? tx.getContractAddress().getData() : tx.getReceiveAddress());
    }

    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(m_endGas)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());

        if (result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * GasCost.getInstance().getSUICIDE_REFUND());
            long gasRefund = Math.min(Math.max(0, result.getFutureRefund()), getGasUsed() / 2);
            byte[] addr = tx.isContractCreation() ? tx.getContractAddress().getData() : tx.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions());


            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();

        // Refund for gas leftover
        track.addBalance(tx.fromPubKeyHash(), summary.getLeftover().add(summary.getRefund()), tx.getAssetId());
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", toHexString(tx.getSender()), summary.getRefund());

        //TODO: Transfer fees to miner
        /*track.addBalance(coinbase, summary.getFee());
        touchedAccounts.add(coinbase);
        logger.info("Pay fees to miner: [{}], feesEarned: [{}]", toHexString(coinbase), summary.getFee());*/

        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(new UInt160(address.getLast20Bytes()));
            }
        }

//        if (blockchainConfig.eip161()) {
        for (byte[] acctAddr : touchedAccounts) {
            Account state = track.getAccount(new UInt160(acctAddr));
            if (state != null && state.isEmpty()) {
                track.delete(new UInt160(acctAddr));
            }
        }
//        }


//        listener.onTransactionExecuted(summary);

        /*if (config.vmTrace() && program != null && result != null) {
            String trace = program.getTrace()
                    .result(result.getHReturn())
                    .error(result.getException())
                    .toString();


            if (config.vmTraceCompressed()) {
                trace = zipAndEncode(trace);
            }

            String txHash = toHexString(tx.getHash());
            saveProgramTraceFile(config, txHash, trace);
            listener.onVMTraceCreated(txHash, trace);
        }*/
        return summary;
    }

    public TransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public TransactionReceipt getReceipt() {
        if (receipt == null) {
            receipt = new TransactionReceipt();
            long totalGasUsed = gasUsedInTheBlock + getGasUsed();
            receipt.setCumulativeGas(totalGasUsed);
            receipt.setTransaction(tx);
            receipt.setLogInfoList(getVMLogs());
            receipt.setGasUsed(getGasUsed());
            receipt.setExecutionResult(getResult().getHReturn());
            receipt.setError(execError);

            receipt.setContractAddress(newContractAddress == null ? EMPTY_BYTE_ARRAY : newContractAddress.getData());

        }
        return receipt;
    }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).subtract(m_endGas).longValue();
    }
}
