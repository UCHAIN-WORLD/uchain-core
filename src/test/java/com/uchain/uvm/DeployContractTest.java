package com.uchain.uvm;

import com.uchain.core.*;
import com.uchain.core.producerblock.ProducerUtil;
import com.uchain.cryptohash.*;
import com.uchain.main.*;
import com.uchain.networkmanager.Node;
import com.uchain.solidity.CallTransaction;
import com.uchain.util.ByteUtil;
import com.uchain.uvm.repository.RepositoryWrapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.uchain.util.TypeConverter.hexToByteArray;
import static org.mockito.Mockito.mock;

public class DeployContractTest {

/*"contract A { " +
            "   event message(string msg);" +
            "   uint public num; " +
            "   function set(uint a) {" +
            "   require(a > 100);" +
            "       num = a; " +
            "       log1(0x1111, 0x2222);" +
            "   }" +
            "   function getPublic() public constant returns (address) {" +
            "        return msg.sender;" +
            "   }" +
            "   function fire() {" +
            "       message(\"fire\");" +
            "   }" +
            "}";*/

    private static final String SRCCODE = "608060405234801561001057600080fd5b5061020f806100206000396000f300608060405260043610610062576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632c04477914610067578063457094cc146100be5780634e70b1dc146100d557806360fe47b114610100575b600080fd5b34801561007357600080fd5b5061007c61012d565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156100ca57600080fd5b506100d3610135565b005b3480156100e157600080fd5b506100ea61019f565b6040518082815260200191505060405180910390f35b34801561010c57600080fd5b5061012b600480360381019080803590602001909291905050506101a5565b005b600033905090565b7f05c766d1c5ea6f40afc38cd8e27308c236c492fbcfa32b458d2755cf76ec1e216040518080602001828103825260048152602001807f666972650000000000000000000000000000000000000000000000000000000081525060200191505060405180910390a1565b60005481565b6064811115156101b457600080fd5b80600081905550612222600102611111604051808260010260001916815260200191505060405180910390a1505600a165627a7a72305820bc2d65db23d3c33d8263809011a0127c09b701f3ae8d3b8ae8a42a0c440f0a4d0029";

    static Settings settings;
    static double _minerAward = 12.3;

//    private static final List<String> forkDirs = new ArrayList<String>();
//    private static final List<ForkBase>  forkDbs    = new ArrayList<ForkBase>();
//
//    private static final List<String> dataDirs = new ArrayList<String>();
//    private static final List<DataBase>  dataDbs    = new ArrayList<DataBase>();
//
//    private static final List<String> blockDirs = new ArrayList<String>();
//    private static final List<BlockBase>  blockDbs    = new ArrayList<BlockBase>();

    static {
        int _produceInterval = 2000;
        Witness witness1 = new Witness("init1",
                "022ac01a1ea9275241615ea6369c85b41e2016abc47485ec616c3c583f1b92a5c8",
                "efc382ccc0358f468c2a80f3738211be98e5ae419fc0907cb2f51d3334001471");
        Witness witness2 = new Witness("init2",
                "03c3333373adc0636b1d67d4bca3d8b34a53d663698119369981e67866250d3a74",
                "cc7b7fa6e706944fa2d75652065f95ef2f364316e172601320655aac0e648165");

        settings = getSettings();

        ArrayList<Witness> witnesses = new ArrayList<>(5);
        witnesses.add(witness1);
        witnesses.add(witness2);
        ConsensusSettings consensusSettings = new ConsensusSettings(witnesses, _produceInterval, 500, 1, 0);
        settings.setConsensusSettings(consensusSettings);
    }

    static public void deleteFile(String sPath) {
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    private static void deleteDir(String dir) {
        try {
            File scFileDir = new File(dir);
            File TrxFiles[] = scFileDir.listFiles();
            for (File curFile : TrxFiles) {
                curFile.delete();
            }
            scFileDir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void cleanUp() throws IOException {

        FileUtils.forceDelete(new File("DeployContractTest"));

    }


    static Settings getSettings() {
        try {
            return new Settings("src/main/resources/config.properties");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    PrivateKey _acct1 = PrivateKey.fromWIF("KwmuSp41VWBtGSWaQQ82ZRRSFzkJVTAyuDLQ9NzP9CPqLWirh4UQ");
    PrivateKey _acct2 = PrivateKey.fromWIF("L32JpLopG2hWjEMSCkAjS1nUnPixVrDTPqFAGYbddQrtUjRfkjEP");
    PrivateKey _acct3 = PrivateKey.fromWIF("KyUTLv2BeP9SJD6Sa8aHBVmuRkgw9eThjNGJDE4PySEgf2TvCQCn");
    PrivateKey _acct4 = PrivateKey.fromWIF("L33Uh9L35pSoEqBPP43U6rQcD2xMpJ7F4b3QMjUMAL6HZhxUqEGq");

    private String minerCoinFrom = "02866facba8742cd702b302021a9588e78b3cd96599a3b1c85688d6dc0a72585e6";

    private Transaction makeTx(PrivateKey from,
                               UInt160 to,
                               Fixed8 amount,
                               Long nonce) {

        val tx = new Transaction(TransactionType.Transfer, from.publicKey().pubKeyHash(), to, "", amount, UInt256.Zero(), nonce,
                new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
        tx.sign(from);
        return tx;
    }

    private Block makeBlock(Block preBlock, List<Transaction> txs) {
        val blockTime = preBlock.getHeader().getTimeStamp() + settings.getConsensusSettings().getProduceInterval();
        val miner = ProducerUtil.getWitness(blockTime, settings.getConsensusSettings());

        val minerTx = new Transaction(TransactionType.Miner, PublicKey.apply(new BinaryData(minerCoinFrom)).pubKeyHash(),
                UInt160.fromBytes(Crypto.hash160(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(miner.getPubkey())))),
                "", Fixed8.fromDecimal(BigDecimal.valueOf(_minerAward)), UInt256.Zero(),
                Long.valueOf(preBlock.height() + 1),
                CryptoUtil.array2binaryData(Crypto.randomBytes(8)), // add random bytes to distinct different blocks with same block index during debug in some cases
                new BinaryData(new ArrayList<>()), 0x01, null);


        val allTxs = new ArrayList<Transaction>();

        allTxs.add(minerTx);
        allTxs.addAll(txs);
        List<UInt256> hashes = new ArrayList<>();
        allTxs.forEach(ele -> {
            hashes.add(ele.id());
        });

        val header = BlockHeader.build(preBlock.getHeader().getIndex() + 1, blockTime, MerkleTree.root(hashes),
                preBlock.id(), PublicKey.apply(CryptoUtil.fromHexString(miner.getPubkey())),
                PrivateKey.apply(CryptoUtil.fromHexString(miner.getPrivkey())));

        return Block.build(header, allTxs);
    }

    private Block makeBlockByTime(Block preBlock,
                                  //txs: Seq[Transaction],
                                  Long blockTime) {
        //val blockTime = preBlock.header.timeStamp + _consensusSettings.produceInterval
        val miner = ProducerUtil.getWitness(blockTime, settings.getConsensusSettings());

        val minerTx = new Transaction(TransactionType.Miner, PublicKey.apply(new BinaryData(minerCoinFrom)).pubKeyHash(),
                UInt160.fromBytes(Crypto.hash160(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(miner.getPubkey())))),
                "", Fixed8.fromDecimal(BigDecimal.valueOf(_minerAward)), UInt256.Zero(),
                Long.valueOf(preBlock.height() + 1),
                CryptoUtil.array2binaryData(Crypto.randomBytes(8)), // add random bytes to distinct different blocks with same block index during debug in some cases
                new BinaryData(new ArrayList<>()), 0x01, null
        );

        val allTxs = new ArrayList<Transaction>();

        allTxs.add(minerTx);
        List<UInt256> hashes = new ArrayList<>();
        allTxs.forEach(ele -> {
            hashes.add(ele.id());
        });

        val header = BlockHeader.build(preBlock.getHeader().getIndex() + 1, blockTime, MerkleTree.root(hashes),
                preBlock.id(), PublicKey.apply(CryptoUtil.fromHexString(miner.getPubkey())),
                PrivateKey.apply(CryptoUtil.fromHexString(miner.getPrivkey())));

        return Block.build(header, allTxs);
    }

    private BlockChain createChain(String path) {
        String baseDir = "DeployContractTest" + path;
        ChainSettings chainSettings = new ChainSettings(new BlockBaseSettings(baseDir + "/block", false, 0),
                new DataBaseSettings(baseDir + "/data", false, 0), new ForkBaseSettings(baseDir + "/fork", false, 0),
                minerCoinFrom, _minerAward, Instant.EPOCH.toEpochMilli(), "03b4534b44d1da47e4b4a504a210401a583f860468dec766f507251a057594e682",
                "7a93d447bffe6d89e690f529a3a0bdff8ff6169172458e04849ef1d4eafd7f86", "[{'addr':'UCgDinSCgoU5DaA6PfwQKzm5kXEVtkQjbKR','coins'='123.12'},{'addr':'UCZS7ZNZK3a8zYkzcJVc3bd17JhJ2SFJu7a','coins'='234.2'}]");

        List<CoinAirdrop> coinAirdrops = new ArrayList<>();
        coinAirdrops.add(new CoinAirdrop(_acct1.publicKey().toAddress(), Double.valueOf(123.12)));
        coinAirdrops.add(new CoinAirdrop(_acct2.publicKey().toAddress(), Double.valueOf(234.2)));
        coinAirdrops.add(new CoinAirdrop(_acct3.publicKey().toAddress(), Double.valueOf(234.2)));
        coinAirdrops.add(new CoinAirdrop(_acct4.publicKey().toAddress(), Double.valueOf(234.2)));

        chainSettings.setCoinAirdrops(coinAirdrops);
        settings.setChainSettings(chainSettings);

        NotificationOnBlock notificationOnBlock = this::onBlock;
        NotificationOnTransaction notificationOnTransaction = this::onTransaction;
        BlockChain blockChain = LevelDBBlockChainBuilder.populate(settings, notificationOnBlock, notificationOnTransaction, node);

        return blockChain;
    }

    Node node = mock(Node.class);


    private void onTransaction(Transaction trx) {
    }

    private void onBlock(Block block) {
    }


    @Test
    public void test() throws Exception {
//
        val chain = createChain("");
        try {

            final byte[] dataBytes = hexToByteArray(SRCCODE);
            Transaction tx = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                tx.sign(_acct1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Repository repository = new RepositoryWrapper(chain);

            executeTransaction(tx, chain, repository, "deploy");

            Transaction tx2 = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 1L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                tx2.sign(_acct1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executeTransaction(tx2, chain, repository, "deploy");

            UInt160 key = tx.getContractAddress();

            Contract contract = ((LevelDBBlockChain) chain).getDataBase().getContract(key);

            contract.getContractMap().forEach((k, v) -> {
                System.out.println("key" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(k.getLast20Bytes())));
                System.out.println("value" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(v.getLast20Bytes())));
            });


            CallTransaction.Function function = CallTransaction.Function.fromSignature("set", "uint");
            Transaction txMethod = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 2L, new BinaryData(CryptoUtil.byteToList(function.encode(0x777))),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            txMethod.sign(_acct1);
            executeTransaction(txMethod, chain, repository, "set");

            contract.getContractMap().forEach((k, v) -> {
                System.out.println("key1" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(k.getLast20Bytes())));
                System.out.println("value1" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(v.getLast20Bytes())));
            });


            CallTransaction.Function function1 = CallTransaction.Function.fromSignature("num");
            Transaction txMethodResult = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 3L, new BinaryData(CryptoUtil.byteToList(function1.encode())),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            txMethodResult.sign(_acct1);

            executeTransaction(txMethodResult, chain, repository, "num");

            CallTransaction.Function functionFailSet = CallTransaction.Function.fromSignature("set", "uint");
            Transaction txMethodSet = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 4L, new BinaryData(CryptoUtil.byteToList(functionFailSet.encode(1))),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            txMethodSet.sign(_acct1);
            executeTransaction(txMethodSet, chain, repository, "set");

            contract.getContractMap().forEach((k, v) -> {
                System.out.println("key1" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(k.getLast20Bytes())));
                System.out.println("value1" + CryptoUtil.toHexString(CryptoUtil.array2binaryData(v.getLast20Bytes())));
            });


            CallTransaction.Function functionGet = CallTransaction.Function.fromSignature("num");
            Transaction txMethodGetResult = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 5L, new BinaryData(CryptoUtil.byteToList(functionGet.encode())),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            txMethodGetResult.sign(_acct1);

            executeTransaction(txMethodGetResult, chain, repository, "num");
        } finally {
            chain.close();
        }
    }

    public void executeTransaction(Transaction tx, BlockChain chain, Repository repository, String method) throws Exception {
        long start = Instant.now().toEpochMilli();
        TransactionReceipt transactionReceipt = ContractApplication.CallContractExcutor(tx, chain, repository);
        transactionReceipt.setTransaction(tx);
        long timeUsed = Instant.now().toEpochMilli() - start;
        System.out.println("------------------------");
        System.out.println("[" + method + "] time used:" + timeUsed + "ms, gas used:" + new BigInteger(1, transactionReceipt.getGasUsed()));
        System.out.println(transactionReceipt);

        System.out.println("++++++++++++++++++++++++");
    }
}
