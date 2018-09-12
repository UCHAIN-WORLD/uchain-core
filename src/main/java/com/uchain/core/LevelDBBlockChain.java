package com.uchain.core;

import com.uchain.core.datastore.*;
import com.uchain.core.datastore.keyvalue.*;
import com.uchain.crypto.*;
import com.uchain.main.ChainSettings;
import com.uchain.main.ConsensusSettings;
import com.uchain.main.Settings;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.iq80.leveldb.WriteBatch;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class LevelDBBlockChain implements BlockChain{

        private static Settings settings = new Settings("config2");

        private ConsensusSettings consensusSettings;

        public LevelDbStorage db = ConnFacory.getInstance(settings.getChainSettings().getChain_dbDir());

        LevelDBBlockChain(ConsensusSettings consensusSettings){
            this.consensusSettings = consensusSettings;
            HeadBlock headBlockStore = headBlkStore.get();
            if(headBlockStore == null) reInit();
            else init(headBlockStore);
        }

//        public LevelDbStorage db = ConnFacory.getInstance(settings.getChain_dbDir());


        private BinaryData genesisProducer = new BinaryData(settings.getChainSettings().getChain_genesis_publicKey()); // TODO: read from settings
        private PrivateKey genesisProducerPrivKey = new PrivateKey(Scalar.apply(new BinaryData(settings.getChainSettings().getChain_genesis_privateKey())));

        private HeaderStore headerStore = new HeaderStore(db, 10, DataStoreConstant.HeaderPrefix,
                new UInt256Key(), new BlockHeaderValue());
        private HeightStore heightStore = new HeightStore(db, 10, DataStoreConstant.HeightToIdIndexPrefix,
                new IntKey(), new UInt256Value());
        private TransactionStore txStore = new TransactionStore(db, 10, DataStoreConstant.TxPrefix,
                new UInt256Key(), new TransactionValue());
        private AccountStore accountStore = new AccountStore(db, 10, DataStoreConstant.AccountPrefix,
                new UInt160Key(), new AccountValue());
        //  private val addressStore = new AddressStore(db)
        private BlkTxMappingStore blkTxMappingStore = new BlkTxMappingStore(db, 10,
                DataStoreConstant.BlockIdToTxIdIndexPrefix, new UInt256Key(), new BlkTxMappingValue());

        private HeadBlockStore headBlkStore = new HeadBlockStore(db, DataStoreConstant.HeadBlockStatePrefix,
                new HeadBlockValue());
        //private val utxoStore = new UTXOStore(db, 10)
        private NameToAccountStore nameToAccountStore = new NameToAccountStore(db, 10,
                DataStoreConstant.NameToAccountIndexPrefix,new StringKey(),new UInt160Key());
        // TODO:  pubkeyNonceStore
        private ProducerStateStore prodStateStore = new ProducerStateStore(db,  DataStoreConstant.ProducerStatePrefix,
                new ProducerStatusValue());


        // TODO: folkBase is todo
        // TODO: zero is not a valid pub key, need to work out other method
        private BinaryData minerCoinFrom = new BinaryData(settings.getChainSettings().getChain_miner());   // 33 bytes pub key
        private Fixed8 minerAward = Fixed8.Ten;

        private UInt160 genesisMinerAddress = UInt160.parse("f54a5851e9372b87810a8e60cdd2e7cfd80b6e31");
        private Transaction genesisTx = new Transaction(TransactionType.Miner, minerCoinFrom,
                genesisMinerAddress, "", minerAward, UInt256Util.Zero(), 0L,
                CryptoUtil.array2binaryData(BinaryData.empty),CryptoUtil.array2binaryData(BinaryData.empty));

        private BlockHeader genesisBlockHeader =  BlockHeader.build(0, settings.getChainSettings().getChain_genesis_timeStamp(),
                UInt256Util.Zero(), UInt256Util.Zero(), genesisProducer, genesisProducerPrivKey);

        private Block genesisBlock = new Block(genesisBlockHeader,Transaction.transactionToArrayList(genesisTx));

        private BlockHeader latestHeader= genesisBlockHeader;

        private ProducerStatus latestProdState = null;

        private BlockHeader initDB(WriteBatch batch){
            BlkTxMapping blkTxMapping = new BlkTxMapping(genesisBlock.id(), genesisBlock.getTransactionIds());
            blkTxMappingStore.set(genesisBlock.id(), blkTxMapping, batch);
            headerStore.set(genesisBlock.id(), genesisBlockHeader, batch);
            heightStore.set(genesisBlock.height(), genesisBlock.id(), batch);
            headBlkStore.set(new HeadBlock(genesisBlockHeader.getIndex(), genesisBlockHeader.id()), batch);
            prodStateStore.set(new ProducerStatus(1L), batch);
            return genesisBlockHeader;
        }

        public BlockHeader reInitDB(WriteBatch batch){
            headerStore.foreachForDelete(batch);
            heightStore.foreachForDelete(batch);
            blkTxMappingStore.foreachForDelete(batch);
            accountStore.foreachForDelete(batch);
            nameToAccountStore.foreachForDelete(batch);
            prodStateStore.delete(batch);
            headBlkStore.delete(batch);
            return initDB(batch);
        }

        public BlockHeader reInit(){
            val batch = db.db.createWriteBatch();
            val ret = reInitDB(batch);
            db.db.write(batch);
            return ret;
        }

        public BlockHeader init(HeadBlock headBlock){
            val blockHeader = headerStore.get(headBlock.getId());
            if(blockHeader == null) return reInit();
            else return blockHeader;
        }


        @Override
        public String getGenesisBlockChainId(){
            return genesisBlock.id() + "";
        }

        @Override
        public BlockChainIterator iterator () {
            return new BlockChainIterator();
        } // to be verified

        @Override
        public BlockHeader getLatestHeader(){
            return genesisBlockHeader;
        }

        @Override
        public int getHeight(){
            return latestHeader.getIndex();
        }

        @Override
        public long getHeadTime(){
            return latestHeader.getTimeStamp();
        }


        @Override
        public long headTimeSinceGenesis(){
            return latestHeader.getTimeStamp() - genesisBlockHeader.getTimeStamp();
        }

        @Override
        public long getDistance(){
            val state = prodStateStore.get();
            assert (state != null);
            return state.getDistance();
        }

        @Override
        public BlockHeader getHeader(UInt256 id){
            return headerStore.get(id);
        }

        @Override
        public BlockHeader getHeader(int index){
            val id = heightStore.get(index);
            if(id != null){
                return getHeader(id);
            }
            return null;
        }

        @Override
        public Block getBlock(UInt256 id){
            val headerBlock = headerStore.get(id);
            if(headerBlock != null){
                val blkTxMapping = blkTxMappingStore.get(headerBlock.id());
                val transactions = new ArrayList<Transaction>(blkTxMapping.getTxIds().size());
                if(blkTxMapping != null){
                    blkTxMapping.getTxIds().forEach(key -> {
                        Transaction transaction = txStore.get(key);
                        if(transaction !=  null) transactions.add(transaction);
                    });
                }
                return new Block(headerBlock, transactions);
            }
            return null;
        }

        @Override
        public Block getBlock(int index){
            val id = heightStore.get(index);
            if(id != null){
                return getBlock(id);
            }
            return null;
        }

        @Override
        public boolean containsBlock(UInt256 id){
            return headerStore.contains(id);
        }

        public boolean saveBlockToStores(Block block){
            try {
            	WriteBatch batch = db.getBatchWrite();
                headerStore.set(block.getHeader().id(), block.getHeader(), batch);
                heightStore.set(block.getHeader().getIndex(), block.getHeader().id(), batch);
                headBlkStore.set(new HeadBlock(block.getHeader().getIndex(), block.getHeader().id()), batch);
                val transations = new ArrayList<UInt256>(block.getTransactions().size());
                block.getTransactions().forEach(transaction -> {transations.add(transaction.id());});
                val blkTxMapping = new BlkTxMapping(block.id(), transations);
                blkTxMappingStore.set(block.id(), blkTxMapping, batch);
                HashMap<UInt160, Account> accounts = new HashMap<UInt160, Account>();
                HashMap<UInt160, HashMap<UInt256, Fixed8>> balances = new HashMap<>();
                block.getTransactions().forEach(tx -> {
                    txStore.set(tx.id(), tx, batch);
                    calcBalancesInBlock(balances, true, tx.fromPubKeyHash(), tx.getAmount(), tx.getAssetId());
                    calcBalancesInBlock(balances, false, tx.getToPubKeyHash(), tx.getAmount(), tx.getAssetId());
                    updateAccout(accounts, tx);
                });
                balances.forEach((accountAddress, balancesInLocalBlk) -> {
                    if(accountStore.contains(accountAddress)){
                        val account = accountStore.get(accountAddress);
                        val updateBalances = updateBalancesInAccount(account.getBalances(), balancesInLocalBlk);
                        val updateAccount = new Account(account.isActive(), account.getName(),updateBalances,
                                account.getNextNonce(), account.getVersion(), account.get_id());
                        accountStore.set(accountAddress, updateAccount, batch);
                    }
                    else{
                        val newAccount = new Account(true, "", getBalancesWithOutAccount(balancesInLocalBlk), 0L);
                        accountStore.set(accountAddress, newAccount, batch);
                    }
                });
                // TODO accounts.foreach()
                latestHeader = block.getHeader();
                db.BatchWrite(batch);
                return true;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return false;
            }
        }

        private HashMap<UInt256, Fixed8> getBalancesWithOutAccount(HashMap<UInt256, Fixed8> balancesInLocalBlk){
            HashMap<UInt256, Fixed8> balancesWithOutAccount = new HashMap<>();
            balancesInLocalBlk.forEach((assetId, balance) -> {
                if(balance.greater(Fixed8.Zero)) balancesWithOutAccount.put(assetId, balance);
            });
            return balancesWithOutAccount;
        }

        private  HashMap<UInt256, Fixed8> updateBalancesInAccount(Map<UInt256, Fixed8> balancesInAccount,
                                                                  HashMap<UInt256, Fixed8> balancesInLocalBlk){
            HashMap<UInt256, Fixed8> updateBalances = new HashMap<>();
            balancesInAccount.forEach((assetId, balance) -> {
                if(balancesInLocalBlk.containsKey(assetId)){
                    updateBalances.put(assetId, balance.add(balancesInLocalBlk.get(assetId)));
                }
                else updateBalances.put(assetId, balance);
            });
            balancesInLocalBlk.forEach((localAssetId, localBalance)-> {
                if(!balancesInAccount.containsKey(localAssetId)){
                    updateBalances.put(localAssetId, localBalance);
                }
            });
            return updateBalances;
        }

        private void updateAccout(Map<UInt160, Account> accounts, Transaction tx){
            // TODO
            return;
        }

        private HashMap<UInt160, HashMap<UInt256, Fixed8>> calcBalancesInBlock( HashMap<UInt160, HashMap<UInt256, Fixed8>> balances, Boolean spent,
                                          UInt160 address, Fixed8 amounts,  UInt256 assetId) {
            Fixed8 amount = amounts;
        if (spent) amount = new Fixed8(0L - amounts.getValue());
        if (balances.containsKey(address)) {
            Map<UInt256, Fixed8> balance = balances.get(address);
            val amountBeforeTrans = balance.get(assetId);
            val amountAfterTrans = new Fixed8(amountBeforeTrans.getValue() + amount.getValue());
            if(balance.containsKey(assetId)) {
                if(balance.containsKey(assetId))
                balance.replace(assetId, amountAfterTrans);
                else balance.put(assetId, amountAfterTrans);
            }
        } else {
            val transRecord = new HashMap<UInt256, Fixed8>();
            transRecord.put(assetId, amount);
            balances.put(address, transRecord);
        }
            return balances;
        }



        @Override
        public Block produceBlock(PublicKey producer, PrivateKey privateKey, long timeStamp,
                                  ArrayList<Transaction> transactions){
            val minerTx = new Transaction(TransactionType.Miner, minerCoinFrom,
                    producer.pubKeyHash(), "", minerAward, UInt256Util.Zero(), new Long((long)(latestHeader.getIndex() + 1)),
                    CryptoUtil.array2binaryData(BinaryData.empty), CryptoUtil.array2binaryData(BinaryData.empty));
            val txs = getUpdateTransaction(minerTx, transactions);
            val merkleRoot = MerkleTree.root(getUpdateTransactionIds(txs));
            val header = BlockHeader.build(latestHeader.getIndex() + 1, timeStamp, merkleRoot,
                    latestHeader.id(), producer.toBin(), privateKey);
            val block = new Block(header, txs);

            if(tryInsertBlock(block))
                return block;
            else return null;
        }

        public ArrayList<Transaction> getUpdateTransaction(Transaction minerTx, ArrayList<Transaction> transactions){
            ArrayList<Transaction> txs = new ArrayList<Transaction>(transactions.size() + 1);
            transactions.forEach(transaction -> {
                if(verifyTransaction(transaction)) txs.add(transaction);
            });
            txs.add(minerTx);
            return txs;
        }

        public ArrayList<UInt256> getUpdateTransactionIds(ArrayList<Transaction> transactions){
            ArrayList<UInt256> txsIds = new ArrayList<UInt256>(transactions.size());
            transactions.forEach(transaction -> {
                if(verifyTransaction(transaction)) txsIds.add(transaction.id());
            });
            return txsIds;
        }

        @Override
        public boolean tryInsertBlock(Block block) {
            if (verifyBlock(block))
                if (saveBlockToStores(block))
                    return true;
            return false;
        }

        @Override
        public Transaction getTransaction(UInt256 id){
            return  txStore.get(id);
        }

        @Override
        public boolean containsTransaction(UInt256 id) {
            return txStore.contains(id);
        }

        @Override
        public boolean verifyBlock(Block block) {
            if (!verifyHeader(block.getHeader()))
                return false;
            if (!forAllTransactionsVerify(block.getTransactions()))
                return false;
            if (!verifyRegisterNames(block.getTransactions()))
                return false;

            return true;
        }

        private boolean verifyRegisterNames(ArrayList<Transaction> transactions){
            val newNames = new ArrayList<String>();
            val registers = new ArrayList<UInt160>();
            for(Transaction tx: transactions){
                if (tx.getTxType() == TransactionType.RegisterName) {
                    String name = "";
                    try {
                        name = new String(CryptoUtil.binaryData2array(tx.getData()), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (newNames.contains(name)) return false;
                    if (registers.contains(tx.fromPubKeyHash())) return false;
                    newNames.add(name);
                    registers.add(tx.fromPubKeyHash());
                }}
            // make sure name is not used
            return forAllNewNamesVerify(newNames) && !forAllRegistersVerify(registers);
            // make sure register never registed before
        }

        private boolean forAllNewNamesVerify(ArrayList<String> names) {
            boolean flag = true;
            for(String name : names){
                if(nameToAccountStore.get(name) == null) {
                    flag = false;
                    return flag;
                }
            }
            return flag;
        }

        private boolean forAllRegistersVerify(ArrayList<UInt160> registers) {
            boolean flag = true;
            for(UInt160 register : registers){
                val account = accountStore.get(register);
                if (account != null && account.getName() != "") {
                    flag = false;
                    return flag;
                }
            }
            return flag;
        }

        private boolean forAllTransactionsVerify(ArrayList<Transaction> transactions) {
            boolean flag = true;
            for(Transaction transaction : transactions){
                if(! verifyTransaction(transaction)) {
                    flag = false;
                    return flag;
                }
            }
            return flag;
        }

        private boolean verifyHeader(BlockHeader header){
            if (header.getIndex() != latestHeader.getIndex() + 1)
                return false;
            if (header.getTimeStamp() < latestHeader.getTimeStamp())
                return false;
            // TODO: verify rule of timeStamp and producer
            if (header.id().equals(latestHeader.id()))
                return false;
            if (!header.getPrevBlock().equals(latestHeader.id()))
                return false;
            if (header.getProducer().getLength() != 33)
                return false;
            if (!header.verifySig())
                return false;

            return true;
        }

        @Override
        public boolean verifyTransaction(Transaction tx) {
            if (tx.getTxType() == TransactionType.Miner) {
                // TODO check miner and only one miner tx
                return true;
            }

            val isInvalid = tx.verifySignature();

            return isInvalid && checkAmount();
        }

        boolean checkAmount(){
            // TODO
            return true;
        }


        @Override
        public Map<UInt256, Long> getBalance(UInt160 address){
            val account = accountStore.get(address);
            if(account == null) return null;
            else{
                if(account.isActive()){
                    Map<UInt256, Long> map = new HashMap<UInt256, Long>();
                    val balanceKeys = account.getBalances().keySet();
                    val balances = account.getBalances();
                    balanceKeys.forEach(key -> {
                        map.put(key, balances.get(key).getValue());
                    });
                }
                return null;
            }
        }

    }
