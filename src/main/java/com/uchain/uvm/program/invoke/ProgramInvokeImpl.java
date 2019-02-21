package com.uchain.uvm.program.invoke;import com.uchain.core.datastore.BlockStore;import com.uchain.uvm.DataWord;import com.uchain.uvm.Repository;import java.math.BigInteger;import java.util.Arrays;import java.util.Map;public class ProgramInvokeImpl implements ProgramInvoke{    private BlockStore blockStore;    /**     * TRANSACTION  env **     */    private final DataWord address;    private final DataWord origin, caller,            balance, gas, gasPrice, callValue;    private final long gasLong;    byte[] msgData;    /**     * BLOCK  env **     */    private final DataWord prevHash, coinbase, timestamp,            number, difficulty, gaslimit;    private Map<DataWord, DataWord> storage;    private Repository repository;    private Repository origRepository;    private boolean byTransaction = true;    private boolean byTestingSuite = false;    private int callDeep = 0;    private boolean isStaticCall = false;    public ProgramInvokeImpl(DataWord address, DataWord origin, DataWord caller, DataWord balance,                             DataWord gasPrice, DataWord gas, DataWord callValue, byte[] msgData,                             DataWord lastHash, DataWord coinbase, DataWord timestamp, DataWord number, DataWord                                     difficulty,                             DataWord gaslimit, Repository repository, Repository origRepository, int callDeep,                             BlockStore blockStore, boolean isStaticCall, boolean byTestingSuite) {        // Transaction env        this.address = address;        this.origin = origin;        this.caller = caller;        this.balance = balance;        this.gasPrice = gasPrice;        this.gas = gas;        this.gasLong = this.gas.longValueSafe();        this.callValue = callValue;        this.msgData = msgData;        // last Block env        this.prevHash = lastHash;        this.coinbase = coinbase;        this.timestamp = timestamp;        this.number = number;        this.difficulty = difficulty;        this.gaslimit = gaslimit;        this.repository = repository;        this.origRepository = origRepository;        this.byTransaction = false;        this.callDeep = callDeep;        this.blockStore = blockStore;        this.isStaticCall = isStaticCall;        this.byTestingSuite = byTestingSuite;    }    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,                             byte[] gaslimit, BlockStore blockStore) {        // Transaction env        this.address = DataWord.of(address);        this.origin = DataWord.of(origin);        this.caller = DataWord.of(caller);        this.balance = DataWord.of(balance);        this.gasPrice = DataWord.of(gasPrice);        this.gas = DataWord.of(gas);        this.gasLong = this.gas.longValueSafe();        this.callValue = DataWord.of(callValue);        this.msgData = msgData;        // last Block env        this.prevHash = DataWord.of(lastHash);        this.coinbase = DataWord.of(coinbase);        this.timestamp = DataWord.of(timestamp);        this.number = DataWord.of(number);        this.difficulty = DataWord.of(difficulty);        this.gaslimit = DataWord.of(gaslimit);        this.blockStore = blockStore;    }    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,                             byte[] gaslimit,                             Repository repository, Repository origRepository, BlockStore blockStore,                             boolean byTestingSuite) {        this(address, origin, caller, balance, gasPrice, gas, callValue, msgData, lastHash, coinbase,                timestamp, number, difficulty, gaslimit, repository, origRepository, blockStore);        this.byTestingSuite = byTestingSuite;    }    public ProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,                             byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,                             byte[] lastHash, byte[] coinbase, long timestamp, long number, byte[] difficulty,                             byte[] gaslimit,                             Repository repository, Repository origRepository, BlockStore blockStore) {        // Transaction env        this.address = DataWord.of(address);        this.origin = DataWord.of(origin);        this.caller = DataWord.of(caller);        this.balance = DataWord.of(balance);        this.gasPrice = DataWord.of(gasPrice);        this.gas = DataWord.of(gas);        this.gasLong = this.gas.longValueSafe();        this.callValue = DataWord.of(callValue);        this.msgData = msgData;        // last Block env        this.prevHash = DataWord.of(lastHash);        this.coinbase = DataWord.of(coinbase);        this.timestamp = DataWord.of(timestamp);        this.number = DataWord.of(number);        this.difficulty = DataWord.of(difficulty);        this.gaslimit = DataWord.of(gaslimit);        this.repository = repository;        this.origRepository = origRepository;        this.blockStore = blockStore;    }    /*           ADDRESS op         */    public DataWord getOwnerAddress() {        return address;    }    /*           BALANCE op         */    public DataWord getBalance() {        return balance;    }    /*           ORIGIN op         */    public DataWord getOriginAddress() {        return origin;    }    /*           CALLER op         */    public DataWord getCallerAddress() {        return caller;    }    /*           GASPRICE op       */    public DataWord getMinGasPrice() {        return gasPrice;    }    /*           GAS op       */    public DataWord getGas() {        return gas;    }    @Override    public long getGasLong() {        return gasLong;    }    /*          CALLVALUE op    */    public DataWord getCallValue() {        return callValue;    }    /*****************/    /***  msg data ***/    /*****************/    private static BigInteger MAX_MSG_DATA = BigInteger.valueOf(Integer.MAX_VALUE);    /*     调用数据加载   */    public DataWord getDataValue(DataWord indexData) {        BigInteger tempIndex = indexData.value();        int index = tempIndex.intValue(); // 可能溢出        int size = 32; // 最大数据流大小        if (msgData == null || index >= msgData.length                || tempIndex.compareTo(MAX_MSG_DATA) == 1)            return DataWord.ZERO;        if (index + size > msgData.length)            size = msgData.length - index;        byte[] data = new byte[32];        System.arraycopy(msgData, index, data, 0, size);        return DataWord.of(data);    }    /*  调用数据大小 */    public DataWord getDataSize() {        if (msgData == null || msgData.length == 0) return DataWord.ZERO;        int size = msgData.length;        return DataWord.of(size);    }    /*  调用数据副本 */    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData) {        int offset = offsetData.intValueSafe();        int length = lengthData.intValueSafe();        byte[] data = new byte[length];        if (msgData == null) return data;        if (offset > msgData.length) return data;        if (offset + length > msgData.length) length = msgData.length - offset;        System.arraycopy(msgData, offset, data, 0, length);        return data;    }    /*     PREVHASH op    */    public DataWord getPrevHash() {        return prevHash;    }    /*     COINBASE op    */    public DataWord getCoinbase() {        return coinbase;    }    /*     TIMESTAMP op    */    public DataWord getTimestamp() {        return timestamp;    }    /*     NUMBER op    */    public DataWord getNumber() {        return number;    }    /*     DIFFICULTY op    */    public DataWord getDifficulty() {        return difficulty;    }    /*     GASLIMIT op    */    public DataWord getGaslimit() {        return gaslimit;    }    /*  Storage */    public Map<DataWord, DataWord> getStorage() {        return storage;    }    public Repository getRepository() {        return repository;    }    /**     * Repository at the start of top-level ttransaction execution     */    public Repository getOrigRepository() {        return origRepository;    }    @Override    public BlockStore getBlockStore() {        return blockStore;    }    @Override    public boolean byTransaction() {        return byTransaction;    }    @Override    public boolean isStaticCall() {        return isStaticCall;    }    @Override    public boolean byTestingSuite() {        return byTestingSuite;    }    @Override    public int getCallDeep() {        return this.callDeep;    }    @Override    public boolean equals(Object o) {        if (this == o) return true;        if (o == null || getClass() != o.getClass()) return false;        ProgramInvokeImpl that = (ProgramInvokeImpl) o;        if (byTestingSuite != that.byTestingSuite) return false;        if (byTransaction != that.byTransaction) return false;        if (address != null ? !address.equals(that.address) : that.address != null) return false;        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;        if (callValue != null ? !callValue.equals(that.callValue) : that.callValue != null) return false;        if (caller != null ? !caller.equals(that.caller) : that.caller != null) return false;        if (coinbase != null ? !coinbase.equals(that.coinbase) : that.coinbase != null) return false;        if (difficulty != null ? !difficulty.equals(that.difficulty) : that.difficulty != null) return false;        if (gas != null ? !gas.equals(that.gas) : that.gas != null) return false;        if (gasPrice != null ? !gasPrice.equals(that.gasPrice) : that.gasPrice != null) return false;        if (gaslimit != null ? !gaslimit.equals(that.gaslimit) : that.gaslimit != null) return false;        if (!Arrays.equals(msgData, that.msgData)) return false;        if (number != null ? !number.equals(that.number) : that.number != null) return false;        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;        if (prevHash != null ? !prevHash.equals(that.prevHash) : that.prevHash != null) return false;        if (repository != null ? !repository.equals(that.repository) : that.repository != null) return false;        if (origRepository != null ? !origRepository.equals(that.origRepository) : that.origRepository != null) return false;        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;        return true;    }    @Override    public String toString() {        return "ProgramInvokeImpl{" +                "address=" + address +                ", origin=" + origin +                ", caller=" + caller +                ", balance=" + balance +                ", gas=" + gas +                ", gasPrice=" + gasPrice +                ", callValue=" + callValue +                ", msgData=" + Arrays.toString(msgData) +                ", prevHash=" + prevHash +                ", coinbase=" + coinbase +                ", timestamp=" + timestamp +                ", number=" + number +                ", difficulty=" + difficulty +                ", gaslimit=" + gaslimit +                ", storage=" + storage +                ", repository=" + repository +                ", origRepository=" + origRepository +                ", byTransaction=" + byTransaction +                ", byTestingSuite=" + byTestingSuite +                ", callDeep=" + callDeep +                '}';    }}