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

public class DeployContractTest1 {

    private static final String SRCCODE = "6060604052341561000f57600080fd5b33600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506101f4600181905550600080819055506105698061006f6000396000f300606060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806313381fbf1461008857806361203265146100d5578063705099b91461012a57806383197ef014610180578063cebe09c914610195578063ec3a6f73146101be578063edca914c146101e7575b600080fd5b341561009357600080fd5b6100bf600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610205565b6040518082815260200191505060405180910390f35b34156100e057600080fd5b6100e861021d565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561013557600080fd5b61016a600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610243565b6040518082815260200191505060405180910390f35b341561018b57600080fd5b61019361040e565b005b34156101a057600080fd5b6101a86104a1565b6040518082815260200191505060405180910390f35b34156101c957600080fd5b6101d16104a7565b6040518082815260200191505060405180910390f35b6101ef6104ad565b6040518082815260200191505060405180910390f35b60036020528060005260406000206000915090505481565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600080600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156102a7576000549150610407565b82600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205414156103be57309050828173ffffffffffffffffffffffffffffffffffffffff16311015156103b6578373ffffffffffffffffffffffffffffffffffffffff166108fc849081150290604051600060405180830381858888f1935050505015156103555760089150610407565b6000600360008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055506000808154809291906001900391905055506000549150610407565b829150610407565b8373ffffffffffffffffffffffffffffffffffffffff166108fc849081150290604051600060405180830381858888f1935050505015156104025760069150610407565b600591505b5092915050565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141561049f57600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16ff5b565b60015481565b60005481565b6000806001546000541015156104c65760019150610539565b34600360003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555060008081548092919060010191905055503090508073ffffffffffffffffffffffffffffffffffffffff163191505b50905600a165627a7a72305820e145143d8fa6f692366071db6c1f51cae5ce329f1a4a613268f59e63c7f5b0b80029";
    private static final String SRCCODE2 = "608060405234801561001057600080fd5b506040516020806101578339810180604052810190808051906020019092919050505060016000819055505061010c8061004b6000396000f300608060405260043610603f576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632f30c6f6146044575b600080fd5b348015604f57600080fd5b50608c60048036038101908080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919050505060ce565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b600082600081905550819050929150505600a165627a7a72305820edd723ab391e3aaa0ba3eae73d6a6ec751d58ce8f04774e25181cecabd33fb070029";


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
                new BinaryData(new ArrayList<>()), new BinaryData(new ArrayList<>()), 0x01, null);
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
        coinAirdrops.add(new CoinAirdrop(_acct1.publicKey().toAddress(), Double.valueOf(123000000.12)));
        coinAirdrops.add(new CoinAirdrop(_acct2.publicKey().toAddress(), Double.valueOf(234000000.2)));
        coinAirdrops.add(new CoinAirdrop(_acct3.publicKey().toAddress(), Double.valueOf(234000000.2)));
        coinAirdrops.add(new CoinAirdrop(_acct4.publicKey().toAddress(), Double.valueOf(234000000.2)));

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


            ConsensusSettings _consensusSettings = ((LevelDBBlockChain) chain).getSettings().getConsensusSettings();
            //assert chain.getHeight() == 0;

            val balance1 = chain.getBalance(_acct1.publicKey().pubKeyHash());

            val balance2 = chain.getBalance(_acct2.publicKey().pubKeyHash());
            //assert (balance2.get(UInt256.Zero()) == Fixed8.fromDecimal(BigDecimal.valueOf(234.2)).getValue());

            long blockTime = chain.getHeadTime() + _consensusSettings.getProduceInterval();
            chain.startProduceBlock(ProducerUtil.getWitness(blockTime, _consensusSettings), blockTime);

            assert (chain.isProducingBlock());


            long block1Time = Instant.now().toEpochMilli();

            val block1 = chain.produceBlockFinalize(block1Time);
            assert block1 != null;
            assert !chain.isProducingBlock();
            assert chain.getHeight() == 1;
            assert chain.getHeadTime() == blockTime;

            List<Transaction> transactions = new ArrayList<>();

            transactions.add(makeTx(_acct3, _acct4.publicKey().pubKeyHash(), Fixed8.fromDecimal(BigDecimal.valueOf(11)), 0L));
            val block2 = makeBlock(block1, transactions);
            chain.tryInsertBlock(block2, true);

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
            TransactionReceipt transactionReceipt = mock(TransactionReceipt.class);
            executeTransaction(tx, chain, repository, "deploy source1");


            List<Transaction> txs = new ArrayList<>();
            txs.add(tx);

            val block3 = makeBlock(block2, txs);
            chain.tryInsertBlock(block3, true);

            //assert chain.getLatestHeader().id() == block3.id();

            transactions.clear();
            transactions.add(makeTx(_acct3, _acct4.publicKey().pubKeyHash(), Fixed8.fromDecimal(BigDecimal.valueOf(0)), 1L));
            val block4 = makeBlock(block3, transactions);
            chain.tryInsertBlock(block4, true);
            //assert chain.getLatestHeader().id() == block4.id();
            transactions.clear();
            transactions.add(makeTx(_acct3, _acct4.publicKey().pubKeyHash(), Fixed8.fromDecimal(BigDecimal.valueOf(0)), 2L));
            val block5 = makeBlock(block4, transactions);
            chain.tryInsertBlock(block5, true);

            //assert chain.getLatestHeader().id() == block5.id();
            Transaction findTx = chain.getTransaction(tx.id());
            //assert findTx.fromAddress().equals(tx.fromAddress());
            //assert findTx.getAmount().equals(tx.getAmount());
            UInt160 key = tx.getContractAddress();
            CallTransaction.Function function = CallTransaction.Function.fromSignature("buyTicket");
            Transaction txMethod = new Transaction(TransactionType.Contract, _acct2.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.TEN), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(function.encode())),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                txMethod.sign(_acct2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executeTransaction(txMethod, chain, repository, "buyTicket");

            transactions.clear();
            transactions.add(txMethod);
            val block6 = makeBlock(block5, transactions);
            chain.tryInsertBlock(block6, true);

            CallTransaction.Function function1 = CallTransaction.Function.fromSignature("numRegistrants");
            Transaction txMethodResult = new Transaction(TransactionType.Contract, _acct2.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 1L, new BinaryData(CryptoUtil.byteToList(function1.encode())),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                txMethodResult.sign(_acct2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executeTransaction(txMethodResult, chain, repository, "numRegistrants");
            transactions.clear();
            transactions.add(txMethodResult);
            val block7 = makeBlock(block6, transactions);
            chain.tryInsertBlock(block7, true);

            CallTransaction.Function function2 = CallTransaction.Function.fromSignature("refundTicket", "address", "uint");
            Transaction txMethodResult2 = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 1L,
                    new BinaryData(CryptoUtil.byteToList(
                            function2.encode(_acct2.publicKey().pubKeyHash().getData(), 1000000000))),//3b9aca00表示Fixed8.fromDecimal(BigDecimal.TEN)
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                txMethodResult2.sign(_acct1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executeTransaction(txMethodResult2, chain, repository, "refundTicket");

            transactions.clear();
            transactions.add(txMethodResult2);
            val block8 = makeBlock(block7, transactions);
            chain.tryInsertBlock(block8, true);

            txMethodResult = new Transaction(TransactionType.Contract, _acct2.publicKey().pubKeyHash(), key, "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 2L, new BinaryData(CryptoUtil.byteToList(function1.encode())),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));
            try {
                txMethodResult.sign(_acct2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            executeTransaction(txMethodResult, chain, repository, "numRegistrants");

            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final byte[] dataBytes2 = hexToByteArray(SRCCODE2);

            Transaction txdepoly = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 2L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(10000)), ByteUtil.hexStringToBytes("0x10000000000"));
            try {
                txdepoly.sign(_acct1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            executeTransaction(txdepoly,chain,repository,"deploy source2");*/

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
        System.out.println(method + " time used:" + timeUsed + "ms, gas used:" + new BigInteger(1, transactionReceipt.getGasUsed()));
        System.out.println(transactionReceipt);

        System.out.println("++++++++++++++++++++++++");
    }
}
