package com.uchain.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uchain.core.block.Block;
import com.uchain.core.block.BlockChain;
import com.uchain.core.block.BlockChainIterator;
import com.uchain.core.block.BlockHeader;
import com.uchain.core.consensus.*;
import com.uchain.core.datastore.*;
import com.uchain.core.datastore.keyvalue.ProducerStatus;
import com.uchain.core.producerblock.ProducerUtil;
import com.uchain.core.transaction.*;
import com.uchain.cryptohash.*;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import com.uchain.networkmanager.Node;
import com.uchain.util.ByteUtil;
import com.uchain.uvm.GasCost;
import com.uchain.uvm.Repository;
import com.uchain.uvm.repository.RepositoryRoot;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.uchain.util.BIUtil.toBI;

@Getter
@Setter
public class LevelDBBlockChain implements BlockChain {
    private static final Logger log = LoggerFactory.getLogger(LevelDBBlockChain.class);
    private Settings settings;
    private ForkBase forkBase;

    private PublicKey genesisProducer;
    private PrivateKey genesisProducerPrivKey;

    private HeaderStore headerStore;
    private HeightStore heightStore;
    private TransactionStore txStore;
    private AccountStore accountStore;
    private BlkTxMappingStore blkTxMappingStore;
    private HeadBlockDataStore headBlkStore;
    private NameToAccountStore nameToAccountStore;
    private ProducerStateStore prodStateStore;
    private PublicKey minerCoinFrom;
    private Fixed8 minerAward;
    private Fixed8 minerFee = Fixed8.Zero;
    private UInt160 genesisCoinToAddress;
    //    private Transaction genesisTx;
//    private BlockHeader genesisBlockHeader;
    private Block genesisBlock;
    private BlockHeader latestHeader;
    private ProducerStatus latestProdState;
    private BlockBase blockBase;
    private DataBase dataBase;
    private TransactionSummaryBase transactionReceiptBase;
    private List<Transaction> pendingTxs = Lists.newArrayList();
    private Map<UInt256, Transaction> unapplyTxs = new HashMap<>();
    private TreeMap<UInt256, TransactionReceipt> txReceipts = new TreeMap<>();
    private PendingState pendingState;
    private NotificationOnBlock notificationOnBlock;
    private NotificationOnTransaction notificationOnTransaction;
    private Node nodeActor;
    private Repository repository;

    LevelDBBlockChain(Settings settings, NotificationOnBlock notificationOnBlock, NotificationOnTransaction notificationOnTransaction, Node node) {
        this.settings = settings;
        this.notificationOnBlock = notificationOnBlock;
        this.notificationOnTransaction = notificationOnTransaction;
        this.nodeActor = node;
        genesisProducer = PublicKey.apply(new BinaryData(settings.getChainSettings().getChain_genesis_publicKey())); // TODO: read from settings
        genesisProducerPrivKey = new PrivateKey(Scalar.apply(new BinaryData(settings.getChainSettings().getChain_genesis_privateKey())));

        blockBase = new BlockBase(settings.getChainSettings().getBlockBaseSettings());
        dataBase = new DataBase(settings.getChainSettings().getDataBaseSettings());
        transactionReceiptBase = new TransactionSummaryBase(settings.getTransactionSummarySettings());
        ConfirmedBlock funcConfirmed = this::onConfirmed;

        OnSwitchBlock funcOnSwitch = this::onSwitch;

        forkBase = new ForkBase(settings, funcConfirmed, funcOnSwitch);

        // TODO: folkBase is todo
        // TODO: zero is not a valid pub key, need to work out other method
        minerCoinFrom = PublicKey.apply(new BinaryData(settings.getChainSettings().getMinerCoinFrom()));   // 33 bytes pub key
        minerAward = Fixed8.fromDouble(settings.getChainSettings().getMinerAward());

        /*genesisCoinToAddress = PublicKeyHash.fromAddress(settings.getChainSettings().getCoinToAddr());
        genesisTx = new Transaction(TransactionType.Miner, minerCoinFrom,
                genesisCoinToAddress, "", minerAward, UInt256.Zero(), 0L,
                settings.getConsensusSettings().fingerprint(),CryptoUtil.array2binaryData(BinaryData.empty),0x01,null);

        genesisBlockHeader =  BlockHeader.build(0, settings.getChainSettings().getChain_genesis_timeStamp(),
                MerkleTree.root(Lists.newArrayList(genesisTx.getId())), UInt256.Zero(), genesisProducer, genesisProducerPrivKey);

        genesisBlock = Block.build(genesisBlockHeader, Transaction.transactionToArrayList(genesisTx));
        latestHeader= genesisBlockHeader;*/
        genesisBlock = buildGenesisBlock();
        latestHeader = genesisBlock.getHeader();
//        List<Transaction> pendingTxs = Lists.newArrayList();  // TODO: save to DB?
//        Map<UInt256, Transaction> unapplyTxs = Maps.newHashMap();  // TODO: save to DB?

        pendingState = new PendingState();

        repository = new RepositoryRoot(forkBase, dataBase, blockBase);

        populate();
    }

    private Block buildGenesisBlock() {
        List<Transaction> genesisTxs = new ArrayList<>();

        this.settings.getChainSettings().getCoinAirdrops().forEach(airdrop ->
                genesisTxs.add(new Transaction(TransactionType.Genesis, minerCoinFrom.pubKeyHash(),
                        PublicKeyHash.fromAddress(airdrop.getAddr()), "", Fixed8.fromDouble(airdrop.getCoins()),
                        UInt256.Zero(), 0L, this.settings.getConsensusSettings().fingerprint(),
                        CryptoUtil.array2binaryData(BinaryData.empty), 0x01, null))
        );

        BlockHeader genesisBlockHeader = BlockHeader.build(0,
                this.settings.getChainSettings().getChain_genesis_timeStamp(), MerkleTree.root(genesisTxs.stream().map(v -> v.id()).collect(Collectors.toList())),
                UInt256.Zero(), genesisProducer, genesisProducerPrivKey);

        return Block.build(genesisBlockHeader, genesisTxs);
    }

    private void populate() {
        if (forkBase.head() == null) {
            applyBlock(genesisBlock, false, false);
            blockBase.add(genesisBlock);
            forkBase.add(genesisBlock);
            nodeActor.blockAddedToHead(genesisBlock);
        }

        assert (forkBase.head() != null);

        resolveSwitchFailure(forkBase.switchState());
        resolveDbUnConsistent(forkBase.head());

        assert (forkBase.head().getBlock().height() >= blockBase.head().getIndex());

        latestHeader = forkBase.head().getBlock().getHeader();
        log.info("populate() latest block " + latestHeader.getIndex() + " " + latestHeader.id());
    }

    @Override
    public String Id() {
        return genesisBlock.id().toString();
    }

    @Override
    public BlockChainIterator iterator() {
        return new BlockChainIterator(this);
    }

    @Override
    public void close() {
        log.info("blockchain closing");
        blockBase.close();
        dataBase.close();
        forkBase.close();
        log.info("blockchain closed");
    }

    @Override
    public ChainInfo getChainInfo() {
        return new ChainInfo(genesisBlock.id().toString());
    }

    @Override
    public int getHeight() {
        if (forkBase.head() != null) {
            return forkBase.head().getBlock().height();
        } else {
            return genesisBlock.height();
        }
    }

    @Override
    public long getHeadTime() {
        if (forkBase.head() != null) {
            return forkBase.head().getBlock().timeStamp();
        } else {
            return 0;
        }
    }

    @Override
    public BlockHeader getLatestHeader() {
        ForkItem forkHead = forkBase.head();
        if (forkHead != null) {
            return forkHead.getBlock().getHeader();
        } else {
            return genesisBlock.getHeader();
        }
    }

    @Override
    public long headTimeSinceGenesis() {
        return getLatestHeader().getTimeStamp() - genesisBlock.getHeader().getTimeStamp();
    }

    @Override
    public BlockHeader getHeader(UInt256 id) {
        if (forkBase.get(id) != null) {
            return forkBase.get(id).getBlock().getHeader();
        } else {
            return blockBase.getBlock(id).getHeader();
        }
    }

    @Override
    public BlockHeader getHeader(int height) {
        if (forkBase.get(height) != null) {
            return forkBase.get(height).getBlock().getHeader();
        } else {
            return blockBase.getBlock(height).getHeader();
        }
    }

    @Override
    public UInt256 getNextBlockId(UInt256 id) {
        UInt256 target = null;
        Block block = getBlock(id);
        if (block != null) {
            Block nextBlock = getBlock(block.height() + 1);
            if (nextBlock != null) {
                target = nextBlock.id();
            }
        }
        if (target == null) {
            target = forkBase.getNext(id);
        }
        return target;
    }


    @Override
    public Block getBlock(UInt256 id) {
        if (UInt256.Zero() == id) {
            return genesisBlock;
        }
        if (forkBase.get(id) != null) {
            return forkBase.get(id).getBlock();
        } else {
            return blockBase.getBlock(id);
        }
    }

    @Override
    public Block getBlock(int height) {
        if (height == 0) {
            return genesisBlock;
        }
        if (forkBase.get(height) != null) {
            return forkBase.get(height).getBlock();
        } else {
            return blockBase.getBlock(height);
        }
    }

    @Override
    public Boolean containsBlock(UInt256 id) {
        return forkBase.contains(id) || blockBase.containBlock(id);
    }

    @Override
    public Transaction getPendingTransaction(UInt256 txid) {
        Optional<Transaction> result = pendingState.getTxs().stream().filter(tx -> tx.getId().equals(txid)).findFirst();
        if (result.isPresent()) {
            return result.get();
        } else {
            return unapplyTxs.get(txid);
        }
    }

    @Override
    public void startProduceBlock(Witness producer, long blockTime) {
        assert (pendingState.getTxs().isEmpty());
        pendingState.set(producer, blockTime);
        long stopProcessTxTime = blockTime - settings.getRuntimeParasStopProcessTxTimeSlot();
        log.info("start block at: " + pendingState.getStartTime());

        ForkItem forkHead = forkBase.head();
        PublicKey publicKey = PublicKey.apply(new BinaryData(producer.getPubkey()));
        Transaction minerTx = new Transaction(TransactionType.Miner, minerCoinFrom.pubKeyHash(),
                publicKey.pubKeyHash(), "", minerFee, UInt256.Zero(), forkHead.getBlock().height() + 1L,
                new BinaryData(CryptoUtil.byteToList(Crypto.randomBytes(8))), // add random bytes to distinct different blocks with same block index during debug in some cases
                new BinaryData(new ArrayList<>()), 0x01, null);
        dataBase.startSession();
        Boolean applied = executeTransaction(minerTx, stopProcessTxTime).isSuccessful();
        assert (applied);
        pendingState.getTxs().add(minerTx);
        addAllPendingTransactions(stopProcessTxTime);
    }

    @Override
    public Boolean addTransaction(Transaction tx) {
        boolean added = false;
        if (toBI(tx.getGasLimit()).compareTo(settings.getRuntimeParasTxAcceptGasLimit()) == 1) {
            added = false;
        } else if (tx.verifySignature()) {
            if (!unapplyTxs.containsKey(tx.id())) {
                unapplyTxs.put(tx.id(), tx);
            }
            added = true;
        }

        if (added) {
            this.nodeActor.addTransaction(tx);
        }
        return added;
    }

    public void addAllPendingTransactions(long stopProcessTxTime) {
        TransactionSorted ret = new TransactionSorted();
        ret.addAll(unapplyTxs.values());
        Iterator<Transaction> it = ret.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            if (isAcceptableTx(tx)) {
                if (Instant.now().toEpochMilli() > stopProcessTxTime) { //当前已经超过可执行时间了，退出
                    break;
                }
                TransactionReceipt receipt = executeTransaction(tx, stopProcessTxTime);
                pendingState.getTxs().add(tx);
                addTransactionReceipt(receipt);
            }
        }
    }

    /**
     *  Gas Price不能低于系统设置的最小值，也不能为负数
     * @param tx
     * @return
     */
    protected boolean isAcceptableTx(Transaction tx) {
        return toBI(tx.getGasPrice()).compareTo(toBI(ByteUtil.hexStringToBytes(GasCost.getInstance().getGasPrice()))) > 0;
    }

    @Override
    public Boolean addTransactionReceipt(TransactionReceipt transactionReceipt) {
        txReceipts.put(transactionReceipt.getTransaction().id(), transactionReceipt);
        return true;
    }

    @Override
    public Boolean isProducingBlock() {
        return !pendingState.getTxs().isEmpty();
    }


    @Override
    public boolean produceBlockAddTransaction(Transaction tx) {
        assert (!pendingTxs.isEmpty());
        TransactionReceipt receipt = executeTransaction(tx, Long.MAX_VALUE);
        if (receipt.isSuccessful()) {
            pendingTxs.add(tx);
            return true;
        } else
            return false;
    }

    @Override
    public Block produceBlockFinalize(long endTime) {
        if (pendingState.getTxs().isEmpty()) {
            log.info("block canceled");
            return null;
        } else {
            log.info("block time: " + pendingState.getBlockTime() + ", end time: " + endTime + ", produce time: " + (endTime - pendingState.getStartTime()));
            ForkItem forkHead = forkBase.head();
            if (!pendingState.getTxs().isEmpty() && pendingState.getTxs().get(0).getTxType() == TransactionType.Miner) {
                pendingState.getTxs().remove(0);
            }
            UInt256 merkleRoot = MerkleTree.nullRoot();
            if (!pendingState.getTxs().isEmpty())
                merkleRoot = MerkleTree.root(pendingState.getTxs().stream().map(v -> v.id()).collect(Collectors.toList()));
            PrivateKey privateKey = new PrivateKey(Scalar.apply(new BinaryData(pendingState.getProducer().getPrivkey())));
            PublicKey publicKey = PublicKey.apply(new BinaryData(pendingState.getProducer().getPubkey()));
            long timeStamp = pendingState.getBlockTime();
            BlockHeader header = BlockHeader.build(
                    forkHead.getBlock().height() + 1, timeStamp, merkleRoot,
                    forkHead.getBlock().id(), publicKey, privateKey);
            Block block = Block.build(header, pendingState.getTxs());
            pendingState.getTxs().clear();
            if (tryInsertBlock(block, false)) {
                notificationOnBlock.onBlock(block);
                nodeActor.newBlockProduce(block);
                return block;
            } else {
                return null;
            }
        }
    }

    public TransactionReceipt executeTransaction(Transaction tx, long stopProcessTxTime) {
        if (tx.getTxType() == TransactionType.Genesis || tx.getTxType() == TransactionType.Miner) {
            applyTransaction(tx);
            return new TransactionReceipt(tx);
        }
        TransactionExecutor executor = new TransactionExecutor(tx, this, repository, stopProcessTxTime);
        executor.init();
        executor.execute();
        executor.go();
        TransactionReceipt receipt = executor.getReceipt();
        return receipt;
    }

    @Override
    public Boolean tryInsertBlock(Block block, Boolean doApply) {
        boolean inserted = false;
        if (!pendingState.getTxs().isEmpty()) {
            pendingState.getTxs().forEach(tx -> {
                if (tx.getTxType() != TransactionType.Miner)
                    unapplyTxs.put(tx.id(), tx);

            });
            pendingState.getTxs().clear();
            dataBase.rollBack();
        }

        if (forkBase.head().getBlock().id().equals(block.prev())) {
            if (doApply == false) { // 本节点出块，直接add 到forkBase
                if (forkBase.add(block)) {
                    inserted = true;
                    latestHeader = block.getHeader();
                } else
                    log.error("Error during forkBase add block " + block.height() + " " + block.id());
            } else if (applyBlock(block, true, true)) { // 来自其它节点传入的Block，apply 所有交易
                if (forkBase.add(block)) {
                    inserted = true;
                    latestHeader = block.getHeader();
                } else
                    log.error("Error during forkBase add block " + block.height() + " " + block.id());
            } else {
                log.info("block ${block.height} ${block.id} apply error");
                //forkBase.removeFork(block.id)
            }

            if (inserted) {
                this.nodeActor.blockAddedToHead(block);
            }
        } else {
            log.info("received block try add to minor fork chain. block " + block.height() + " " + block.id());
            if (forkBase.add(block))
                inserted = true;
            else
                log.info("add fail");
        }
        if (inserted) {
            block.getTransactions().forEach(tx -> {
                unapplyTxs.remove(tx.id());
            });
        }
        return inserted;
    }

    @Override
    public Transaction getTransaction(UInt256 id) {
        TransactionReceipt transactionReceipt = null;
        if (id != null)
            transactionReceipt = transactionReceiptBase.getTransactionReceipt(id);
        if (transactionReceipt != null) {
            Block block = getBlock(transactionReceipt.getBlockHeight());
            Transaction tx = block.getTransaction(transactionReceipt.getTxIndex());
            return tx;
        } else {
            return null;
        }
    }

    @Override
    public TransactionReceipt getTransactionReceipt(UInt256 id) {
        TransactionReceipt transactionReceipt = null;
        if (id != null)
            transactionReceipt = transactionReceiptBase.getTransactionReceipt(id);
        return transactionReceipt;
    }

    @Override
    public boolean containsTransaction(UInt256 id) {
        return getTransaction(id) != null;
    }

    private Boolean applyBlock(Block block, boolean verify, boolean enableSession) {
        boolean applied = true;
        if (!verify || verifyBlock(block)) {
            if (enableSession) {
                dataBase.startSession();
            }
            for (Transaction tx : block.getTransactions()) {
                TransactionReceipt receipt = executeTransaction(tx, Long.MAX_VALUE);
                addTransactionReceipt(receipt);
            }
        } else
            applied = false;
        if (!applied) {
            log.info("Block apply fail " + block.height() + " " + block.id());
        }
        return applied;
    }


    private Boolean applyTransaction(Transaction tx) {
        boolean txValid = true;

        Account fromAccount = dataBase.getAccount(tx.fromPubKeyHash());

        Map<UInt256, Fixed8> frombalances = Maps.newHashMap();
        if (fromAccount == null) {
            fromAccount = new Account(true, "", frombalances, 0L, 0x01, null);
        }
        Account toAccount = dataBase.getAccount(tx.getToPubKeyHash());
        Map<UInt256, Fixed8> tobalances = Maps.newHashMap();
        if (toAccount == null) {
            toAccount = new Account(true, "", tobalances, 0L, 0x01, null);
        }

//        if (tx.getTxType() == TransactionType.Miner || tx.getTxType() == TransactionType.Genesis) {
//        } else {
//            if (!fromAccount.getBalances().containsKey(tx.getAssetId())) txValid = false;
//            val txAmount = tx.getAmount();
//            val fromBalances = fromAccount.getBalances();
//            val fromAmount = fromBalances.get(tx.getAssetId()) == null ? new Fixed8(0) : fromBalances.get(tx.getAssetId());
//            if (txAmount.greater(fromAmount)) txValid = false;
//            if (!tx.getNonce().equals(fromAccount.getNextNonce())) txValid = false;
//        }
        try {
            if (tx.getTxType() != TransactionType.Miner) {   //only genesis ?
                Map<UInt256, Fixed8> fromBalance = updateBalancesAccount(fromAccount.getBalances(), tx, "mus");
                Map<UInt256, Fixed8> toBalance = updateBalancesAccount(toAccount.getBalances(), tx, "add");
                fromAccount.setBalances(fromBalance);
                fromAccount.IncNonce();
                toAccount.setBalances(toBalance);
                dataBase.setAccount(tx.fromPubKeyHash(), fromAccount, tx.getToPubKeyHash(), toAccount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            txValid = false;
        }

        return txValid;
    }


    private Map<UInt256, Fixed8> updateBalancesAccount(Map<UInt256, Fixed8> balancesInAccount,
                                                       Transaction tx, String flag) {
        Fixed8 amount = tx.getAmount();
        if (flag.equals("mus")) {
            amount = Fixed8.Zero.mus(tx.getAmount());
        }
        val balance = balancesInAccount.containsKey(tx.getAssetId()) ? balancesInAccount.get(tx.getAssetId()).add(amount) : amount;
        balancesInAccount.put(tx.getAssetId(), balance);

        return balancesInAccount;
    }

    private Map<UInt256, Fixed8> updateBalancesContractAccount(Map<UInt256, Fixed8> balancesInAccount,
                                                               Transaction tx, Fixed8 gasTotal, String flag) throws IOException {
        Fixed8 amount = tx.getAmount();

        if (flag.equals("mus")) {
            amount = Fixed8.Zero.mus(tx.getAmount().add(gasTotal));
        }
        val balance = balancesInAccount.containsKey(tx.getAssetId()) ? balancesInAccount.get(tx.getAssetId()).add(amount) : amount;
        balancesInAccount.put(tx.getAssetId(), balance);

        return balancesInAccount;
    }


    private Boolean verifyBlock(Block block) {
        if (!verifyHeader(block.getHeader()))
            return false;
        else if (block.getTransactions().size() == 0) {
            if (block.getHeader().getMerkleRoot().equals(UInt256.Zero())) {
                return true;
            } else {
                log.info("verifyBlock error: block.transactions.size == 0");
                return false;
            }
        } else if (!block.merkleRoot().equals(block.getHeader().getMerkleRoot()))
            return false;
        else if (!verifyTxs(block.getTransactions()))
            return false;
        else if (!verifyRegisterNames(block.getTransactions()))
            return false;
        else
            return true;
    }

    private Boolean verifyTxs(List<Transaction> txs) {
        boolean isValid = true;
        int txsNum = txs.size();
        for (int i = 0; i < txsNum; i++) {
            if (!verifyTransaction(txs.get(i))) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    private Boolean verifyTransaction(Transaction tx) {
        if (tx.getTxType() == TransactionType.Miner || tx.getTxType() == TransactionType.Genesis) {
            return true;
        } else {
            boolean isValid = tx.verifySignature();
            return isValid && checkAmount();
        }
    }


    private Boolean verifyHeader(BlockHeader header) {
        val prevBlock = forkBase.get(header.getPrevBlock());
        if (prevBlock == null) {
            log.info("verifyHeader error: prevBlock not found");
            return false;
        } else if (header.getIndex() != prevBlock.getBlock().height() + 1) {
            log.info("verifyHeader error: index error " + header.getIndex() + " " + prevBlock.getBlock().height());
            return false;
        } else if (!ProducerUtil.isProducerValid(header.getTimeStamp(), header.getProducer(), settings.getConsensusSettings())) {
            log.info("verifyHeader error: producer not valid");
            return false;
        } else if (!header.verifySig()) {
            log.info("verifyHeader error: verifySig fail");
            return false;
        } else {
            // verify merkleRoot in verifyBlock()
            return true;
        }
    }


    private boolean verifyRegisterNames(List<Transaction> transactions) {
        boolean isValid = true;
        Set<String> newNames = new HashSet();
        Set<UInt160> registers = new HashSet();
        int txNum = transactions.size();
        for (int i = 0; i < txNum; i++) {
            Transaction tx = transactions.get(i);
            if (tx.getTxType() == TransactionType.RegisterName) {
                String name = "";
                try {
                    name = new String(CryptoUtil.binaryData2array(tx.getData()), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (name.length() != 10) {// TODO: read "10" from config file
                    isValid = false;
                }
                if (newNames.contains(name)) {
                    isValid = false;
                }
                if (registers.contains(tx.fromPubKeyHash())) {
                    isValid = false;
                }
                newNames.add(name);
                registers.add(tx.fromPubKeyHash());
            }
        }
        Iterator newNamesIt = newNames.iterator();
        while (newNamesIt.hasNext()) {
            if (dataBase.nameExists((String) newNamesIt.next())) {
                isValid = false;
                break;
            }
        }

        Iterator registersIt = registers.iterator();
        while (newNamesIt.hasNext()) {
            if (dataBase.registerExists((UInt160) registersIt.next())) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    boolean checkAmount() {
        return true;
    }

    @Override
    public Map<UInt256, BigInteger> getBalance(UInt160 address) {
        Map<UInt256, BigInteger> resultMap = Maps.newHashMap();
        Map<UInt256, Fixed8> map = dataBase.getBalance(address);
        map.forEach((k, v) -> resultMap.put(k, v.getValue()));
        return resultMap;
    }

    @Override
    public Account getAccount(UInt160 address) {
        return dataBase.getAccount(address);
    }


    private void onConfirmed(Block block) {
        if (block.height() > 0) {
            log.info("confirm block " + block.height() + " (" + block.id() + ")");
            dataBase.commit(block.height());
            blockBase.add(block);

            int len = block.getTransactions().size();
            List<TransactionReceipt> transactionReceipts = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                Transaction tx = block.getTransaction(i);
                TransactionReceipt transactionReceipt = txReceipts.get(tx.id());
                try {
                    transactionReceipt.setBlockHeight(block.height());
                    transactionReceipt.setBlockHash(block.id().getData());
                    transactionReceipt.setTxIndex(i);
                    transactionReceipts.add(transactionReceipt);
                    txReceipts.remove(tx.id());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("receipt exception.");
                }
            }
            if (!transactionReceipts.isEmpty()) {
                transactionReceiptBase.add(transactionReceipts);
            }
            transactionReceiptBase.commit();
        }
        nodeActor.confirmedBlock(block);
    }

    private void printChain(String title, List<ForkItem> fork) {
        String str = fork.stream().map(item -> item.getBlock().id().toString().substring(0, 6)).collect(Collectors.joining("->"));
        log.info(title + ": " + str);
    }

    private void resolveSwitchFailure(SwitchState state) {
        if (state == null) return;
        val oldBranch = forkBase.getBranch(state.getOldHead(), state.getForkPoint());
        val newBranch = forkBase.getBranch(state.getNewHead(), state.getForkPoint());
        val result = onSwitch(oldBranch, newBranch, state);
        forkBase.endSwitch(oldBranch, newBranch, result);
    }

    private void resolveDbUnConsistent(ForkItem head) {
        while (dataBase.revision() > head.height() + 1) {
            dataBase.rollBack();
        }
    }

    private SwitchResult onSwitch(List<ForkItem> from, List<ForkItem> to, SwitchState switchState) {
        printChain("old chain", from);
        printChain("new chain", to);
//        from.forEach(forkItem -> dataBase.rollBack());
//        to.forEach(forkItem -> applyBlock(forkItem.getBlock()));

        assert (dataBase.revision() == from.get(from.size() - 1).height() + 1);
        while (dataBase.revision() > switchState.getHeight() + 1) {
            dataBase.rollBack();
        }

        int appliedCount = 0;
        for (ForkItem forkItem : to) {
            if (applyBlock(forkItem.getBlock(), true, true)) {
                appliedCount += 1;
            }
        }
        SwitchResult switchResult;
        if (appliedCount < to.size()) {
            while (dataBase.revision() > switchState.getHeight() + 1) {
                dataBase.rollBack();
            }
            from.forEach(item -> applyBlock(item.getBlock(), true, true));
            switchResult = new SwitchResult(false, to.get(appliedCount));
        } else {
            nodeActor.forkSwitch(from, to);
            switchResult = new SwitchResult(true, null);
        }
        return switchResult;
    }

}
