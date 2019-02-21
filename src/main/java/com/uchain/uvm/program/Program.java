package com.uchain.uvm.program;

import com.uchain.core.Account;
import com.uchain.core.Transaction;
import com.uchain.core.datastore.Source;
import com.uchain.cryptohash.HashUtil;
import com.uchain.cryptohash.UInt160;
import com.uchain.cryptohash.UInt256;
import com.uchain.main.SystemProperties;
import com.uchain.util.*;
import com.uchain.uvm.*;
import com.uchain.uvm.hook.VMHook;
import com.uchain.uvm.program.invoke.ProgramInvoke;
import com.uchain.uvm.program.invoke.ProgramInvokeFactory;
import com.uchain.uvm.program.invoke.ProgramInvokeFactoryImpl;
import com.uchain.uvm.program.listener.CompositeProgramListener;
import com.uchain.uvm.program.listener.ProgramListenerAware;
import com.uchain.uvm.program.listener.ProgramStorageChangeListener;
import com.uchain.uvm.trace.ProgramTrace;
import com.uchain.uvm.trace.ProgramTraceListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;

import static com.uchain.util.BIUtil.isNotCovers;
import static com.uchain.util.ByteUtil.toHexString;
import static java.lang.Math.min;
import static java.lang.String.format;
import static java.math.BigInteger.ZERO;
import static org.apache.commons.lang.ArrayUtils.*;

@Getter
@Setter
public class Program {

    private static final Logger logger = LoggerFactory.getLogger("VM");
    /**
     * 此属性定义VM中允许的递归调用的数目。
     * 注意：JVM在没有堆栈溢出异常的情况下达到这个级别，
     * VM可能需要用JVM参数启动来增加
     * 堆栈大小。例如：XSS10M
     */
    private static final int MAX_DEPTH = 1024;

    //堆栈大小
    private static final int MAX_STACKSIZE = 1024;

    private Transaction transaction;

    private ProgramInvoke invoke;
    private ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();

    private ProgramOutListener listener;
    private ProgramTraceListener traceListener;
    private ProgramStorageChangeListener storageDiffListener = new ProgramStorageChangeListener();
    private CompositeProgramListener programListener = new CompositeProgramListener();

    private Stack stack;
    private Memory memory;
    private Storage storage;
    private Repository originalRepo;
    private byte[] returnDataBuffer;

    private ProgramResult result = new ProgramResult();
    private ProgramTrace trace = new ProgramTrace();

    private byte[] codeHash;
    private byte[] ops;
    private int pc;
    private byte lastOp;
    private byte previouslyExecutedOp;
    private boolean stopped;
    private ByteArraySet touchedAccounts = new ByteArraySet();

    private ProgramPrecompile programPrecompile;
//    CommonConfig commonConfig = CommonConfig.getDefault();
    Source<byte[], ProgramPrecompile> precompileSource;
    private final SystemProperties config;
    //private final BlockchainConfig blockchainConfig;
    private final VMHook vmHook;
    private long stopProcessTxTime;

    public Program(byte[] ops, ProgramInvoke programInvoke,long stopProcessTxTime) {
        this(ops, programInvoke, (Transaction) null,stopProcessTxTime);
    }

    public Program(byte[] ops, ProgramInvoke programInvoke, SystemProperties config,long stopProcessTxTime) {
        this(ops, programInvoke, null, config, VMHook.EMPTY,stopProcessTxTime);
    }

    public Program(byte[] ops, ProgramInvoke programInvoke, Transaction transaction,long stopProcessTxTime) {
        this(ops, programInvoke, transaction, SystemProperties.getDefault(), VMHook.EMPTY,stopProcessTxTime);
    }

    public Program(byte[] ops, ProgramInvoke programInvoke, Transaction transaction, SystemProperties config, VMHook vmHook,long stopProcessTxTime) {
        this(null, ops, programInvoke, transaction, config, vmHook,stopProcessTxTime);
    }

    public Program(byte[] codeHash, byte[] ops, ProgramInvoke programInvoke, Transaction transaction, SystemProperties config, VMHook vmHook,long stopProcessTxTime) {
        this.config = config;
        this.invoke = programInvoke;
        this.transaction = transaction;

        this.codeHash = codeHash == null || FastByteComparisons.equal(HashUtil.EMPTY_DATA_HASH, codeHash) ? null : codeHash;
        this.ops = nullToEmpty(ops);

        this.vmHook = vmHook;
        this.traceListener = new ProgramTraceListener(config.isVmTrace());
        this.memory = setupProgramListener(new Memory());
        this.stack = setupProgramListener(new Stack());
        this.originalRepo = programInvoke.getOrigRepository();
        this.storage = setupProgramListener(new Storage(programInvoke));
        this.trace = new ProgramTrace(config, programInvoke);
        this.stopProcessTxTime = stopProcessTxTime;
    }

    public ProgramPrecompile getProgramPrecompile() {
        if (programPrecompile == null) {
            if (codeHash != null && precompileSource != null) {
                programPrecompile = precompileSource.get(codeHash);
            }
            if (programPrecompile == null) {
                programPrecompile = ProgramPrecompile.compile(ops);

                if (codeHash != null && precompileSource != null) {
                    precompileSource.put(codeHash, programPrecompile);
                }
            }
        }
        return programPrecompile;
    }
//
//    public Program withCommonConfig(CommonConfig commonConfig) {
//        this.commonConfig = commonConfig;
//        return this;
//    }

    public int getCallDeep() {
        return invoke.getCallDeep();
    }

    private InternalTransaction addInternalTx(byte[] nonce, DataWord gasLimit, byte[] senderAddress, byte[] receiveAddress,
                                              BigInteger value, byte[] data, String note) {

        InternalTransaction result = null;
        if (transaction != null) {
            byte[] senderNonce = isEmpty(nonce) ?
                    getStorage().getNonce(new UInt160(senderAddress)).toByteArray() : nonce;

            data = config.isRecordInternalTransactionsData() ? data : null;
            result = getResult().addInternalTransaction(transaction.id().getData(), getCallDeep(), senderNonce,
                    getGasPrice(), gasLimit, senderAddress, receiveAddress, value.toByteArray(), data, note);
        }

        return result;
    }

    private <T extends ProgramListenerAware> T setupProgramListener(T programListenerAware) {
        if (programListener.isEmpty()) {
            programListener.addListener(traceListener);
            programListener.addListener(storageDiffListener);
        }
        programListenerAware.setProgramListener(programListener);
        return programListenerAware;
    }

    public Map<DataWord, DataWord> getStorageDiff() {
        return storageDiffListener.getDiff();
    }

    public byte getOp(int pc) {
        return (getLength(ops) <= pc) ? 0 : ops[pc];
    }

    public byte getCurrentOp() {
        return isEmpty(ops) ? 0 : ops[pc];
    }

    /**
     * 最后的OP只能公开设置（没有getLastOp方法），用于日志记录
     */
    public void setLastOp(byte op) {
        this.lastOp = op;
    }

    /**
     * 应在OP完全执行后设置
     */
    public void setPreviouslyExecutedOp(byte op) {
        this.previouslyExecutedOp = op;
    }

    /**
     * 返回最后完全执行的OP
     */
    public byte getPreviouslyExecutedOp() {
        return this.previouslyExecutedOp;
    }

    public void stackPush(byte[] data) {
        stackPush(DataWord.of(data));
    }

    public void stackPushZero() {
        stackPush(DataWord.ZERO);
    }

    public void stackPushOne() {
        DataWord stackWord = DataWord.ONE;
        stackPush(stackWord);
    }

    public void stackPush(DataWord stackWord) {
        verifyStackOverflow(0, 1); //完整性检查
        stack.push(stackWord);
    }

    public Stack getStack() {
        return this.stack;
    }

    public int getPC() {
        return pc;
    }

    public void setPC(DataWord pc) {
        this.setPC(pc.intValue());
    }

    public void setPC(int pc) {
        this.pc = pc;
        if (this.pc >= ops.length) {
            stop();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
    }

    public void setHReturn(byte[] buff) {
        getResult().setHReturn(buff);
    }

    public void step() {
        setPC(pc + 1);
    }

    public byte[] sweep(int n) {
        if (pc + n > ops.length)
            stop();
        byte[] data = Arrays.copyOfRange(ops, pc, pc + n);
        pc += n;
        if (pc >= ops.length) stop();
        return data;
    }

    public DataWord stackPop() {
        return stack.pop();
    }

    /**
     * 验证堆栈至少是stackSize大小
     */
    public void verifyStackSize(int stackSize) {
        if (stack.size() < stackSize) {
            throw Program.Exception.tooSmallStack(stackSize, stack.size());
        }
    }

    public void verifyStackOverflow(int argsReqs, int returnReqs) {
        if ((stack.size() - argsReqs + returnReqs) > MAX_STACKSIZE) {
            throw new StackTooLargeException("Expected: overflow " + MAX_STACKSIZE + " elements stack limit");
        }
    }

    public int getMemSize() {
        return memory.size();
    }

    public void memorySave(DataWord addrB, DataWord value) {
        memory.write(addrB.intValue(), value.getData(), value.getData().length, false);
    }

    public void memorySaveLimited(int addr, byte[] data, int dataSize) {
        memory.write(addr, data, dataSize, true);
    }

    public void memorySave(int addr, byte[] value) {
        memory.write(addr, value, value.length, false);
    }

    public void memoryExpand(DataWord outDataOffs, DataWord outDataSize) {
        if (!outDataSize.isZero()) {
            memory.extend(outDataOffs.intValue(), outDataSize.intValue());
        }
    }

    /**
     * 分配一块内存并在给定的偏移地址存储值
     *
     * @param addr      偏移地址
     * @param allocSize 需要写入内存的大小
     * @param value     写入内存的数据
     */
    public void memorySave(int addr, int allocSize, byte[] value) {
        memory.extendAndWrite(addr, allocSize, value);
    }


    public DataWord memoryLoad(DataWord addr) {
        return memory.readWord(addr.intValue());
    }

    public DataWord memoryLoad(int address) {
        return memory.readWord(address);
    }

    public byte[] memoryChunk(int offset, int size) {
        return memory.read(offset, size);
    }

    /**
     * 在程序中分配额外的内存,从给定偏移量计算的指定大小
     *
     * @param offset 内存地址偏移
     * @param size   要分配的字节数
     */
    public void allocateMemory(int offset, int size) {
        memory.extend(offset, size);
    }

    public void suicide(DataWord obtainerAddress) {
        byte[] owner = getOwnerAddress().getData();
        byte[] obtainer = obtainerAddress.getData();
        BigInteger balance = getStorage().getBalance(new UInt160(owner), UInt256.assetId);

        if (logger.isInfoEnabled())
            logger.info("Transfer to: [{}] heritage: [{}]",
                    toHexString(obtainer),
                    balance);

        addInternalTx(null, null, owner, obtainer, balance, null, "suicide");
        if (FastByteComparisons.compareTo(owner, 0, 32, obtainer, 0, 32) == 0) {
            getStorage().addBalance(new UInt160(owner), balance.negate(), UInt256.assetId);
        } else {
            BIUtil.transfer(getStorage(), owner, obtainer, balance);
        }
        getResult().addDeleteAccount(this.getOwnerAddress());
    }

    public Repository getStorage() {
        return this.storage;
    }

    /**
     * 创建智能合约
     * @param value         Endowment
     * @param memStart      Code memory offset
     * @param memSize       Code memory size
     */
    public void createContract(DataWord value, DataWord memStart, DataWord memSize) {
        returnDataBuffer = null; // 在调用之前重置返回缓冲区

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        BigInteger endowment = value.value();
        if (!verifyCall(senderAddress, endowment))
            return;

        byte[] nonce = getStorage().getNonce(new UInt160(senderAddress)).toByteArray();
        byte[] contractAddress = HashUtil.calcNewAddr(senderAddress, nonce);

        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());
        createContractImpl(value, programCode, contractAddress);
    }

    public void createContract2(DataWord value, DataWord memStart, DataWord memSize, DataWord salt) {
        returnDataBuffer = null; // 在调用之前重置返回缓冲区

        byte[] senderAddress = this.getOwnerAddress().getData();
        BigInteger endowment = value.value();
        if (!verifyCall(senderAddress, endowment))
            return;

        byte[] programCode = memoryChunk(memStart.intValue(), memSize.intValue());
        byte[] contractAddress = HashUtil.calcSaltAddr(senderAddress, programCode, salt.getData());

        createContractImpl(value, programCode, contractAddress);
    }

    /**
     * 验证创建尝试
     */
    private boolean verifyCall(byte[] senderAddress, BigInteger endowment) {
        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            return false;
        }
        if (isNotCovers(getStorage().getBalance(new UInt160(senderAddress), UInt256.assetId), endowment)) {
            stackPushZero();
            return false;
        }
        return true;
    }

    /**
     * All stages required to create contract on provided address after initial check
     * @param value         Endowment
     * @param programCode   Contract code
     * @param newAddress    Contract address
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void createContractImpl(DataWord value, byte[] programCode, byte[] newAddress) {

        // [1] LOG, SPEND GAS
        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        if (logger.isInfoEnabled())
            logger.info("creating a new contract inside contract run: [{}]", toHexString(senderAddress));

//        BlockchainConfig blockchainConfig = config.getBlockchainConfig().getConfigForBlock(getNumber().longValue());
        //  actual gas subtract
//        DataWord gasLimit = blockchainConfig.getCreateGas(getGas());
        DataWord gasLimit = getGas();
        spendGas(gasLimit.longValue(), "internal call");

        // [2] CREATE THE CONTRACT ADDRESS
        Account existingAddr = getStorage().getAccount(new UInt160(DataWord.of(newAddress).getLast20Bytes()));
        boolean contractAlreadyExists = existingAddr != null /*&& existingAddr.isContractExist()*/;

        if (byTestingSuite()) {
            // This keeps track of the contracts created for a test
            getResult().addCallCreate(programCode, EMPTY_BYTE_ARRAY,
                    gasLimit.getNoLeadZeroesData(),
                    value.getNoLeadZeroesData());
        }

        // [3] UPDATE THE NONCE
        // (THIS STAGE IS NOT REVERTED BY ANY EXCEPTION)
        if (!byTestingSuite()) {
            getStorage().increaseNonce(new UInt160(senderAddress));
        }

        Repository track = getStorage();

        //In case of hashing collisions, check for any balance before createAccount()
        BigInteger oldBalance = track.getBalance(new UInt160(newAddress), UInt256.assetId);
        track.createAccount(new UInt160(DataWord.of(newAddress).getLast20Bytes()));
        if (config.isEip161()) {
            track.increaseNonce(new UInt160(DataWord.of(newAddress).getLast20Bytes()));
        }
        track.addBalance(new UInt160(DataWord.of(newAddress).getLast20Bytes()), oldBalance, UInt256.assetId);

        // [4] TRANSFER THE BALANCE
        BigInteger endowment = value.value();
        BigInteger newBalance = ZERO;
        if (!byTestingSuite()) {
            track.addBalance(new UInt160(senderAddress), endowment.negate(), UInt256.assetId);
            newBalance = track.addBalance(new UInt160(DataWord.of(newAddress).getLast20Bytes()), endowment, UInt256.assetId);
        }


        // [5] COOK THE INVOKE AND EXECUTE
        byte[] nonce = getStorage().getNonce(new UInt160(senderAddress)).toByteArray();
        InternalTransaction internalTx = addInternalTx(nonce, getGasLimit(), senderAddress, null, endowment, programCode, "create");
        ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                this, DataWord.of(newAddress), getOwnerAddress(), value, gasLimit,
                newBalance, null, track, this.invoke.getOrigRepository(), this.invoke.getBlockStore(), false, byTestingSuite());

        ProgramResult result = ProgramResult.createEmpty();

        if (contractAlreadyExists) {
            result.setException(new BytecodeExecutionException("Trying to create a contract with existing contract address: 0x" + toHexString(newAddress)));
        } else if (isNotEmpty(programCode)) {
            VM vm = new VM(config, vmHook);
            Program program = new Program(programCode, programInvoke, internalTx, config, vmHook,stopProcessTxTime);
            vm.play(program);
            result = program.getResult();
        }

        // 4. CREATE THE CONTRACT OUT OF RETURN
        byte[] code = result.getHReturn();

        long storageCost = getLength(code) * GasCost.getInstance().getCREATE_DATA();
        long afterSpend = programInvoke.getGas().longValue() - storageCost - result.getGasUsed();
        if (afterSpend < 0) {
            if (!new Constants().createEmptyContractOnOOG()) {
                result.setException(Program.Exception.notEnoughSpendingGas("No gas to return just created contract",
                        storageCost, this));
            } else {
                track.saveCode(new UInt160(newAddress), EMPTY_BYTE_ARRAY);
            }
        } else if (getLength(code) > new Constants().getMAX_CONTRACT_SZIE()) {
            result.setException(Program.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                    storageCost, this));
        } else if (!result.isRevert()){
            result.spendGas(storageCost);
            track.saveCode(new UInt160(newAddress), code);
        }

        getResult().merge(result);

        if (result.getException() != null || result.isRevert()) {
            logger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                    toHexString(newAddress),
                    result.getException());

            internalTx.reject();
            result.rejectInternalTransactions();

            track.rollback();
            stackPushZero();

            if (result.getException() != null) {
                return;
            } else {
                returnDataBuffer = result.getHReturn();
            }
        } else {
            if (!byTestingSuite())
                track.commit();

            // IN SUCCESS PUSH THE ADDRESS INTO THE STACK
            stackPush(DataWord.of(newAddress));
        }

        // 5. REFUND THE REMAIN GAS
        long refundGas = gasLimit.longValue() - result.getGasUsed();
        if (refundGas > 0) {
            refundGas(refundGas, "remain gas from the internal call");
            if (logger.isInfoEnabled()) {
                logger.info("The remaining gas is refunded, account: [{}], gas: [{}] ",
                        toHexString(getOwnerAddress().getLast20Bytes()),
                        refundGas);
            }
        }
        touchedAccounts.add(newAddress);
    }


    public void callToAddress(MessageCall msg) {
        returnDataBuffer = null; // reset return buffer right before the call

        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            refundGas(msg.getGas().longValue(), " call deep limit reach");
            return;
        }

        byte[] data = memoryChunk(msg.getInDataOffs().intValue(), msg.getInDataSize().intValue());

        // FETCH THE SAVED STORAGE
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] senderAddress = getOwnerAddress().getLast20Bytes();
        byte[] contextAddress = msg.getType().callIsStateless() ? senderAddress : codeAddress;

        if (logger.isInfoEnabled())
            logger.info(msg.getType().name() + " for existing contract: address: [{}], outDataOffs: [{}], outDataSize: [{}]  ",
                    toHexString(contextAddress), msg.getOutDataOffs().longValue(), msg.getOutDataSize().longValue());

        Repository track = getStorage();
        // 2.1 PERFORM THE VALUE (endowment) PART
        BigInteger endowment = msg.getEndowment().value();
        BigInteger senderBalance = track.getBalance(new UInt160(senderAddress), UInt256.assetId);
        if (isNotCovers(senderBalance, endowment)) {
            stackPushZero();
            refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }


        // FETCH THE CODE
        byte[] programCode = getStorage().isExist(new UInt160(codeAddress)) ?
                getStorage().getCode(new UInt160(codeAddress)) : EMPTY_BYTE_ARRAY;


        BigInteger contextBalance = ZERO;
        if (byTestingSuite()) {
            // This keeps track of the calls created for a test
            getResult().addCallCreate(data, contextAddress,
                    msg.getGas().getNoLeadZeroesData(),
                    msg.getEndowment().getNoLeadZeroesData());
        } else {
            track.addBalance(new UInt160(senderAddress), endowment.negate(), UInt256.assetId);
            contextBalance = track.addBalance(new UInt160(contextAddress), endowment, UInt256.assetId);
        }

        // CREATE CALL INTERNAL TRANSACTION
        InternalTransaction internalTx = addInternalTx(null, getGasLimit(), senderAddress, contextAddress, endowment, data, "call");

        ProgramResult result = null;
        if (isNotEmpty(programCode)) {
            ProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(
                    this, DataWord.of(contextAddress),
                    msg.getType().callIsDelegate() ? getCallerAddress() : getOwnerAddress(),
                    msg.getType().callIsDelegate() ? getCallValue() : msg.getEndowment(),
                    msg.getGas(), contextBalance, data, track, this.invoke.getOrigRepository(),
                    this.invoke.getBlockStore(),
                    msg.getType().callIsStatic() || isStaticCall(), byTestingSuite());

            VM vm = new VM(config, vmHook);
            Program program = new Program(getStorage().getCodeHash(new UInt160(codeAddress)),
                    programCode, programInvoke, internalTx,config, vmHook,stopProcessTxTime);
            vm.play(program);
            result = program.getResult();

            getTrace().merge(program.getTrace());
            getResult().merge(result);

            if (result.getException() != null || result.isRevert()) {
                logger.debug("contract run halted by Exception: contract: [{}], exception: [{}]",
                        toHexString(contextAddress),
                        result.getException());

                internalTx.reject();
                result.rejectInternalTransactions();

                track.rollback();
                stackPushZero();

                if (result.getException() != null) {
                    return;
                }
            } else {
                // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
                track.commit();
                stackPushOne();
            }

            if (byTestingSuite()) {
                logger.info("Testing run, skipping storage diff listener");
            } else if (Arrays.equals(transaction.getReceiveAddress(), internalTx.getReceiveAddress())) {
                storageDiffListener.merge(program.getStorageDiff());
            }
        } else {
            // 4. THE FLAG OF SUCCESS IS ONE PUSHED INTO THE STACK
            track.commit();
            stackPushOne();
        }

        // 3. APPLY RESULTS: result.getHReturn() into out_memory allocated
        if (result != null) {
            byte[] buffer = result.getHReturn();
            int offset = msg.getOutDataOffs().intValue();
            int size = msg.getOutDataSize().intValue();

            memorySaveLimited(offset, buffer, size);

            returnDataBuffer = buffer;
        }

        // 5. REFUND THE REMAIN GAS
        if (result != null) {
            BigInteger refundGas = msg.getGas().value().subtract(BIUtil.toBI(result.getGasUsed()));
            if (BIUtil.isPositive(refundGas)) {
                refundGas(refundGas.longValue(), "remaining gas from the internal call");
                if (logger.isInfoEnabled())
                    logger.info("The remaining gas refunded, account: [{}], gas: [{}] ",
                            toHexString(senderAddress),
                            refundGas.toString());
            }
        } else {
            refundGas(msg.getGas().longValue(), "remaining gas from the internal call");
        }
    }


    public void spendGas(long gasValue, String cause) {
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] Spent for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);
        }

        if (getGasLong() < gasValue) {
            throw Program.Exception.notEnoughSpendingGas(cause, gasValue, this);
        }
        getResult().spendGas(gasValue);
    }

    public void checkStopTime(){
        if (Instant.now().toEpochMilli() > stopProcessTxTime) {
            logger.error("Error: vm execution timeout");
            //TODO: rollBack
//            throw new OutOfBlockTimeException("Error: vm execution timeout "+stopProcessTxTime);
        }
    }

    public void spendAllGas() {
        spendGas(getGas().longValue(), "Spending all remaining");
    }

    public void refundGas(long gasValue, String cause) {
        logger.info("[{}] Refund for cause: [{}], gas: [{}]", invoke.hashCode(), cause, gasValue);
        getResult().refundGas(gasValue);
    }

    public void futureRefundGas(long gasValue) {
        logger.info("Future refund added: [{}]", gasValue);
        getResult().addFutureRefund(gasValue);
    }

    public void resetFutureRefund() {
        getResult().resetFutureRefund();
    }

    public void storageSave(DataWord word1, DataWord word2) {
        storageSave(word1.getData(), word2.getData());
    }

    public void storageSave(byte[] key, byte[] val) {
        DataWord keyWord = DataWord.of(key);
        DataWord valWord = DataWord.of(val);
        byte[] contractAddress = getOwnerAddress().getLast20Bytes();
       getStorage().addStorageRow(new UInt160(contractAddress), keyWord, valWord);
    }

    public byte[] getCode() {
        return ops;
    }

    public byte[] getCodeAt(DataWord address) {
        byte[] code = invoke.getRepository().getCode(new UInt160(address.getLast20Bytes()));
        return nullToEmpty(code);
    }

    public byte[] getCodeHashAt(DataWord address) {
        Account account = invoke.getRepository().getAccount(new UInt160(address.getLast20Bytes()));
        if (account != null && account.isEmpty()){
            return EMPTY_BYTE_ARRAY;
        }else{
            byte[] code = invoke.getRepository().getCodeHash(new UInt160(address.getLast20Bytes()));
            return nullToEmpty(code);
        }
    }

    public DataWord getOwnerAddress() {
        return invoke.getOwnerAddress();
    }

    public DataWord getBlockHash(int index) {
        return index < this.getNumber().longValue() && index >= Math.max(256, this.getNumber().intValue()) - 256 ?
                DataWord.of(this.invoke.getBlockStore().get(new UInt256(getPrevHash().getData())).getId().getData()/*.getBlockHashByNumber(index, getPrevHash().getData())*/) :
                DataWord.ZERO;
    }

    public DataWord getBalance(DataWord address) {
        BigInteger balance = getStorage().getBalance(new UInt160(address.getLast20Bytes()), UInt256.assetId);
        return DataWord.of(balance.toByteArray());
    }

    public DataWord getOriginAddress() {
        return invoke.getOriginAddress();
    }

    public DataWord getCallerAddress() {
        return invoke.getCallerAddress();
    }

    public DataWord getGasPrice() {
        return invoke.getMinGasPrice();
    }

    public long getGasLong() {
        return invoke.getGasLong() - getResult().getGasUsed();
    }

    public DataWord getGas() {
        return DataWord.of(invoke.getGasLong() - getResult().getGasUsed());
    }

    public DataWord getCallValue() {
        return invoke.getCallValue();
    }

    public DataWord getDataSize() {
        return invoke.getDataSize();
    }

    public DataWord getDataValue(DataWord index) {
        return invoke.getDataValue(index);
    }

    public byte[] getDataCopy(DataWord offset, DataWord length) {
        return invoke.getDataCopy(offset, length);
    }

    public DataWord getReturnDataBufferSize() {
        return DataWord.of(getReturnDataBufferSizeI());
    }

    private int getReturnDataBufferSizeI() {
        return returnDataBuffer == null ? 0 : returnDataBuffer.length;
    }

    public byte[] getReturnDataBufferData(DataWord off, DataWord size) {
        if ((long) off.intValueSafe() + size.intValueSafe() > getReturnDataBufferSizeI()) return null;
        return returnDataBuffer == null ? new byte[0] :
                Arrays.copyOfRange(returnDataBuffer, off.intValueSafe(), off.intValueSafe() + size.intValueSafe());
    }

    public DataWord storageLoad(DataWord key) {
        //return DataWord.ZERO;
        return getStorage().getStorageValue(new UInt160(getOwnerAddress().getLast20Bytes()), key);
    }

    public DataWord getCurrentValue(DataWord key) {
        return getStorage().getStorageValue(new UInt160(getOwnerAddress().getLast20Bytes()), key);
    }

    public DataWord getOriginalValue(DataWord key) {
        return originalRepo.getStorageValue(new UInt160(getOwnerAddress().getLast20Bytes()), key);
    }

    public DataWord getPrevHash() {
        return invoke.getPrevHash();
    }

    public DataWord getCoinbase() {
        return invoke.getCoinbase();
    }

    public DataWord getTimestamp() {
        return invoke.getTimestamp();
    }

    public DataWord getNumber() {
        return invoke.getNumber();
    }

//    public BlockchainConfig getBlockchainConfig() {
//        return blockchainConfig;
//    }

    public DataWord getDifficulty() {
        return invoke.getDifficulty();
    }

    public DataWord getGasLimit() {
        return invoke.getGaslimit();
    }

    public boolean isStaticCall() {
        return invoke.isStaticCall();
    }

    public ProgramResult getResult() {
        return result;
    }

    public void setRuntimeFailure(RuntimeException e) {
        getResult().setException(e);
    }

    public String memoryToString() {
        return memory.toString();
    }

    public void fullTrace() {

        if (logger.isTraceEnabled() || listener != null) {

            StringBuilder stackData = new StringBuilder();
            for (int i = 0; i < stack.size(); ++i) {
                stackData.append(" ").append(stack.get(i));
                if (i < stack.size() - 1) stackData.append("\n");
            }

            if (stackData.length() > 0) stackData.insert(0, "\n");

            ContractDetails contractDetails = getStorage().
                    getContractDetails(new UInt160(getOwnerAddress().getLast20Bytes()));
            StringBuilder storageData = new StringBuilder();
            if (contractDetails != null) {
                try {
                    List<DataWord> storageKeys = new ArrayList<>(contractDetails.getStorage().keySet());
                    Collections.sort(storageKeys);
                    for (DataWord key : storageKeys) {
                        storageData.append(" ").append(key).append(" -> ").
                                append(contractDetails.getStorage().get(key)).append("\n");
                    }
                    if (storageData.length() > 0) storageData.insert(0, "\n");
                } catch (java.lang.Exception e) {
                    storageData.append("Failed to print storage: ").append(e.getMessage());
                }
            }

            StringBuilder memoryData = new StringBuilder();
            StringBuilder oneLine = new StringBuilder();
            if (memory.size() > 320)
                memoryData.append("... Memory Folded.... ")
                        .append("(")
                        .append(memory.size())
                        .append(") bytes");
            else
                for (int i = 0; i < memory.size(); ++i) {

                    byte value = memory.readByte(i);
                    oneLine.append(ByteUtil.oneByteToHexString(value)).append(" ");

                    if ((i + 1) % 16 == 0) {
                        String tmp = format("[%4s]-[%4s]", Integer.toString(i - 15, 16),
                                Integer.toString(i, 16)).replace(" ", "0");
                        memoryData.append("").append(tmp).append(" ");
                        memoryData.append(oneLine);
                        if (i < memory.size()) memoryData.append("\n");
                        oneLine.setLength(0);
                    }
                }
            if (memoryData.length() > 0) memoryData.insert(0, "\n");

            StringBuilder opsString = new StringBuilder();
            for (int i = 0; i < ops.length; ++i) {

                String tmpString = Integer.toString(ops[i] & 0xFF, 16);
                tmpString = tmpString.length() == 1 ? "0" + tmpString : tmpString;

                if (i != pc)
                    opsString.append(tmpString);
                else
                    opsString.append(" >>").append(tmpString).append("");

            }
            if (pc >= ops.length) opsString.append(" >>");
            if (opsString.length() > 0) opsString.insert(0, "\n ");

            logger.trace(" -- OPS --     {}", opsString);
            logger.trace(" -- STACK --   {}", stackData);
            logger.trace(" -- MEMORY --  {}", memoryData);
            logger.trace(" -- STORAGE -- {}\n", storageData);
            logger.trace("\n  Spent Gas: [{}]/[{}]\n  Left Gas:  [{}]\n",
                    getResult().getGasUsed(),
                    invoke.getGas().longValue(),
                    getGas().longValue());

            StringBuilder globalOutput = new StringBuilder("\n");
            if (stackData.length() > 0) stackData.append("\n");

            if (pc != 0)
                globalOutput.append("[Op: ").append(OpCode.code(lastOp).name()).append("]\n");

            globalOutput.append(" -- OPS --     ").append(opsString).append("\n");
            globalOutput.append(" -- STACK --   ").append(stackData).append("\n");
            globalOutput.append(" -- MEMORY --  ").append(memoryData).append("\n");
            globalOutput.append(" -- STORAGE -- ").append(storageData).append("\n");

            if (getResult().getHReturn() != null)
                globalOutput.append("\n  HReturn: ").append(
                        Hex.toHexString(getResult().getHReturn()));

            // sophisticated assumption that msg.data != codedata
            // means we are calling the contract not creating it
            byte[] txData = invoke.getDataCopy(DataWord.ZERO, getDataSize());
            if (!Arrays.equals(txData, ops))
                globalOutput.append("\n  msg.data: ").append(Hex.toHexString(txData));
            globalOutput.append("\n\n  Spent Gas: ").append(getResult().getGasUsed());

            if (listener != null)
                listener.output(globalOutput.toString());
        }
    }

    public void saveOpTrace() {
        if (this.pc < ops.length) {
            trace.addOp(ops[pc], pc, getCallDeep(), getGas(), traceListener.resetActions());
        }
    }

    public ProgramTrace getTrace() {
        return trace;
    }

    static String formatBinData(byte[] binData, int startPC) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < binData.length; i += 16) {
//            ret.append(Utils.align("" + Integer.toHexString(startPC + (i)) + ":", ' ', 8, false));
            ret.append(Hex.toHexString(binData, i, min(16, binData.length - i))).append('\n');
        }
        return ret.toString();
    }

    public static String stringifyMultiline(byte[] code) {
        int index = 0;
        StringBuilder sb = new StringBuilder();
        BitSet mask = buildReachableBytecodesMask(code);
        ByteArrayOutputStream binData = new ByteArrayOutputStream();
        int binDataStartPC = -1;

        while (index < code.length) {
            final byte opCode = code[index];
            OpCode op = OpCode.code(opCode);

            if (!mask.get(index)) {
                if (binDataStartPC == -1) {
                    binDataStartPC = index;
                }
                binData.write(code[index]);
                index++;
                if (index < code.length) continue;
            }

            if (binDataStartPC != -1) {
                sb.append(formatBinData(binData.toByteArray(), binDataStartPC));
                binDataStartPC = -1;
                binData = new ByteArrayOutputStream();
                if (index == code.length) continue;
            }

//            sb.append(Utils.align("" + Integer.toHexString(index) + ":", ' ', 8, false));

            if (op == null) {
                sb.append("<UNKNOWN>: ").append(0xFF & opCode).append("\n");
                index++;
                continue;
            }

            if (op.name().startsWith("PUSH")) {
                sb.append(' ').append(op.name()).append(' ');

                int nPush = op.val() - OpCode.PUSH1.val() + 1;
                byte[] data = Arrays.copyOfRange(code, index + 1, index + nPush + 1);
                BigInteger bi = new BigInteger(1, data);
                sb.append("0x").append(bi.toString(16));
                if (bi.bitLength() <= 32) {
                    sb.append(" (").append(new BigInteger(1, data).toString()).append(") ");
                }

                index += nPush + 1;
            } else {
                sb.append(' ').append(op.name());
                index++;
            }
            sb.append('\n');
        }

        return sb.toString();
    }

    static class ByteCodeIterator {
        byte[] code;
        int pc;

        public ByteCodeIterator(byte[] code) {
            this.code = code;
        }

        public void setPC(int pc) {
            this.pc = pc;
        }

        public int getPC() {
            return pc;
        }

        public OpCode getCurOpcode() {
            return pc < code.length ? OpCode.code(code[pc]) : null;
        }

        public boolean isPush() {
            return getCurOpcode() != null ? getCurOpcode().name().startsWith("PUSH") : false;
        }

        public byte[] getCurOpcodeArg() {
            if (isPush()) {
                int nPush = getCurOpcode().val() - OpCode.PUSH1.val() + 1;
                byte[] data = Arrays.copyOfRange(code, pc + 1, pc + nPush + 1);
                return data;
            } else {
                return new byte[0];
            }
        }

        public boolean next() {
            pc += 1 + getCurOpcodeArg().length;
            return pc < code.length;
        }
    }

    static BitSet buildReachableBytecodesMask(byte[] code) {
        NavigableSet<Integer> gotos = new TreeSet<>();
        ByteCodeIterator it = new ByteCodeIterator(code);
        BitSet ret = new BitSet(code.length);
        int lastPush = 0;
        int lastPushPC = 0;
        do {
            ret.set(it.getPC()); // reachable bytecode
            if (it.isPush()) {
                lastPush = new BigInteger(1, it.getCurOpcodeArg()).intValue();
                lastPushPC = it.getPC();
            }
            if (it.getCurOpcode() == OpCode.JUMP || it.getCurOpcode() == OpCode.JUMPI) {
                if (it.getPC() != lastPushPC + 1) {
                    ret.set(0, code.length);
                    return ret;
                }
                int jumpPC = lastPush;
                if (!ret.get(jumpPC)) {
                    gotos.add(jumpPC);
                }
            }
            if (it.getCurOpcode() == OpCode.JUMP || it.getCurOpcode() == OpCode.RETURN ||
                    it.getCurOpcode() == OpCode.STOP) {
                if (gotos.isEmpty()) break;
                it.setPC(gotos.pollFirst());
            }
        } while (it.next());
        return ret;
    }

    public static String stringify(byte[] code) {
        int index = 0;
        StringBuilder sb = new StringBuilder();
        BitSet mask = buildReachableBytecodesMask(code);
        String binData = "";

        while (index < code.length) {
            final byte opCode = code[index];
            OpCode op = OpCode.code(opCode);

            if (op == null) {
                sb.append(" <UNKNOWN>: ").append(0xFF & opCode).append(" ");
                index++;
                continue;
            }

            if (op.name().startsWith("PUSH")) {
                sb.append(' ').append(op.name()).append(' ');

                int nPush = op.val() - OpCode.PUSH1.val() + 1;
                byte[] data = Arrays.copyOfRange(code, index + 1, index + nPush + 1);
                BigInteger bi = new BigInteger(1, data);
                sb.append("0x").append(bi.toString(16)).append(" ");

                index += nPush + 1;
            } else {
                sb.append(' ').append(op.name());
                index++;
            }
        }

        return sb.toString();
    }


    public void addListener(ProgramOutListener listener) {
        this.listener = listener;
    }

    public int verifyJumpDest(DataWord nextPC) {
        if (nextPC.bytesOccupied() > 4) {
            throw Program.Exception.badJumpDestination(-1);
        }
        int ret = nextPC.intValue();
        if (!getProgramPrecompile().hasJumpDest(ret)) {
            throw Program.Exception.badJumpDestination(ret);
        }
        return ret;
    }

    public void callToPrecompiledAddress(MessageCall msg, PrecompiledContracts.PrecompiledContract contract) {
        returnDataBuffer = null; // reset return buffer right before the call

        if (getCallDeep() == MAX_DEPTH) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), " call deep limit reach");
            return;
        }

        Repository track = getStorage();

        byte[] senderAddress = this.getOwnerAddress().getLast20Bytes();
        byte[] codeAddress = msg.getCodeAddress().getLast20Bytes();
        byte[] contextAddress = msg.getType().callIsStateless() ? senderAddress : codeAddress;


        BigInteger endowment = msg.getEndowment().value();
        BigInteger senderBalance = track.getBalance(new UInt160(senderAddress), UInt256.assetId);
        if (senderBalance.compareTo(endowment) < 0) {
            stackPushZero();
            this.refundGas(msg.getGas().longValue(), "refund gas from message call");
            return;
        }

        byte[] data = this.memoryChunk(msg.getInDataOffs().intValue(),
                msg.getInDataSize().intValue());

        // Charge for endowment - is not reversible by rollback
        BIUtil.transfer(track, senderAddress, contextAddress, endowment);

        if (byTestingSuite()) {
            // This keeps track of the calls created for a test
            this.getResult().addCallCreate(data,
                    msg.getCodeAddress().getLast20Bytes(),
                    msg.getGas().getNoLeadZeroesData(),
                    msg.getEndowment().getNoLeadZeroesData());

            stackPushOne();
            return;
        }


        long requiredGas = contract.getGasForData(data);
        if (requiredGas > msg.getGas().longValue()) {

            this.refundGas(0, "call pre-compiled"); //matches cpp logic
            this.stackPushZero();
            track.rollback();
        } else {

            if (logger.isDebugEnabled())
                logger.debug("Call {}(data = {})", contract.getClass().getSimpleName(), toHexString(data));

            Pair<Boolean, byte[]> out = contract.execute(data);

            if (out.getLeft()) { // success
                this.refundGas(msg.getGas().longValue() - requiredGas, "call pre-compiled");
                this.stackPushOne();
                returnDataBuffer = out.getRight();
                track.commit();
            } else {
                // spend all gas on failure, push zero and revert state changes
                this.refundGas(0, "call pre-compiled");
                this.stackPushZero();
                track.rollback();
            }

            this.memorySave(msg.getOutDataOffs().intValue(), msg.getOutDataSize().intValueSafe(), out.getRight());
        }
    }

    public boolean byTestingSuite() {
        return invoke.byTestingSuite();
    }

    public interface ProgramOutListener {
        void output(String out);
    }

    @SuppressWarnings("serial")
    public static class BytecodeExecutionException extends RuntimeException {
        public BytecodeExecutionException(String message) {
            super(message);
        }
    }

    @SuppressWarnings("serial")
    public static class OutOfGasException extends BytecodeExecutionException {

        public OutOfGasException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class IllegalOperationException extends BytecodeExecutionException {

        public IllegalOperationException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class BadJumpDestinationException extends BytecodeExecutionException {

        public BadJumpDestinationException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class StackTooSmallException extends BytecodeExecutionException {

        public StackTooSmallException(String message, Object... args) {
            super(format(message, args));
        }
    }

    @SuppressWarnings("serial")
    public static class ReturnDataCopyIllegalBoundsException extends BytecodeExecutionException {
        public ReturnDataCopyIllegalBoundsException(DataWord off, DataWord size, long returnDataSize) {
            super(String.format("Illegal RETURNDATACOPY arguments: offset (%s) + size (%s) > RETURNDATASIZE (%d)", off, size, returnDataSize));
        }
    }

    @SuppressWarnings("serial")
    public static class StaticCallModificationException extends BytecodeExecutionException {
        public StaticCallModificationException() {
            super("Attempt to call a state modifying opcode inside STATICCALL");
        }
    }


    public static class Exception {

        public static OutOfGasException notEnoughOpGas(OpCode op, long opGas, long programGas) {
            return new OutOfGasException("Not enough gas for '%s' operation executing: opGas[%d], programGas[%d];", op, opGas, programGas);
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, DataWord opGas, DataWord programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughOpGas(OpCode op, BigInteger opGas, BigInteger programGas) {
            return notEnoughOpGas(op, opGas.longValue(), programGas.longValue());
        }

        public static OutOfGasException notEnoughSpendingGas(String cause, long gasValue, Program program) {
            return new OutOfGasException("Not enough gas for '%s' cause spending: invokeGas[%d], gas[%d], usedGas[%d];",
                    cause, program.invoke.getGas().longValue(), gasValue, program.getResult().getGasUsed());
        }

        public static OutOfGasException gasOverflow(BigInteger actualGas, BigInteger gasLimit) {
            return new OutOfGasException("Gas value overflow: actualGas[%d], gasLimit[%d];", actualGas.longValue(), gasLimit.longValue());
        }

        public static IllegalOperationException invalidOpCode(byte... opCode) {
            return new IllegalOperationException("Invalid operation code: opCode[%s];", Hex.toHexString(opCode, 0, 1));
        }

        public static BadJumpDestinationException badJumpDestination(int pc) {
            return new BadJumpDestinationException("Operation with pc isn't 'JUMPDEST': PC[%d];", pc);
        }

        public static StackTooSmallException tooSmallStack(int expectedSize, int actualSize) {
            return new StackTooSmallException("Expected stack size %d but actual %d;", expectedSize, actualSize);
        }
    }

    @SuppressWarnings("serial")
    public class StackTooLargeException extends BytecodeExecutionException {
        public StackTooLargeException(String message) {
            super(message);
        }
    }

    /**
     * used mostly for testing reasons
     */
    public byte[] getMemory() {
        return memory.read(0, memory.size());
    }

    public void initMem(byte[] data) {
        this.memory.write(0, data, data.length, false);
    }
}
