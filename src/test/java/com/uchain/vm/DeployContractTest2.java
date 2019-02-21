package com.uchain.vm;

import com.uchain.core.*;
import com.uchain.core.producer.ProducerUtil;
import com.uchain.crypto.*;
import com.uchain.main.*;
import com.uchain.network.Node;
import com.uchain.solidity.compiler.CompilationResult;
import com.uchain.solidity.compiler.SolidityCompiler;
import com.uchain.util.ByteUtil;
import com.uchain.vm.repository.RepositoryWrapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.uchain.solidity.compiler.SolidityCompiler.Options.*;
import static com.uchain.util.TypeConverter.hexToByteArray;
import static org.mockito.Mockito.mock;

public class DeployContractTest2 {


    public static String contractCompile(String fileName, String contractName) throws IOException {
        Path source = Paths.get("src", "test", "resources", fileName);
        SolidityCompiler.Result res = SolidityCompiler.compile(source.toFile(), true, ABI, BIN, INTERFACE, METADATA);
        CompilationResult result = CompilationResult.parse(res.output);

        CompilationResult.ContractMetadata a = result.getContract(source, contractName);
        // System.out.println(a.bin);

        return a.bin;

    }

    static Settings settings;
    static double _minerAward = 12.3;

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
        ConsensusSettings consensusSettings = new ConsensusSettings(witnesses, _produceInterval, 500, 1,0);
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

        FileUtils.forceDelete(new File("DeployContractTest3"));

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


    private BlockChain createChain(String path) {
        String baseDir = "DeployContractTest3" + path;
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

    public void deployContract(Transaction tx, BlockChain chain, Repository repository,String contractName) throws Exception {

        long start = Instant.now().toEpochMilli();
        TransactionReceipt transactionReceipt = ContractApplication.CallContractExcutor(tx, chain, repository);
        transactionReceipt.setTransaction(tx);
        long timeUsed = Instant.now().toEpochMilli() - start;
        System.out.println("------------------------");
        System.out.println("["+contractName+"] deploy time used:" + timeUsed + "ms, gas used:" + new BigInteger(1, transactionReceipt.getGasUsed()));
        System.out.println(transactionReceipt);

        System.out.println("++++++++++++++++++++++++");
    }

    @Test
    public void test() throws Exception {
        val chain = createChain("");
        try {
            ConsensusSettings _consensusSettings = ((LevelDBBlockChain) chain).getSettings().getConsensusSettings();
            long blockTime = chain.getHeadTime() + _consensusSettings.getProduceInterval();
            chain.startProduceBlock(ProducerUtil.getWitness(blockTime, _consensusSettings), blockTime);

            long block1Time = Instant.now().toEpochMilli();
            chain.produceBlockFinalize(block1Time);

            byte[] dataBytes = hexToByteArray(contractCompile("ERC20Token.sol", "Wealth"));
            Transaction tx = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            tx.sign(_acct1);
            Repository repository = new RepositoryWrapper(chain);

            deployContract(tx,chain,repository,"erc20-token");

            Transaction tx2 = new Transaction(TransactionType.Contract, _acct1.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 1L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            tx2.sign(_acct1);
            deployContract(tx2,chain,repository,"erc20-token");

            //第二个合约

            dataBytes = hexToByteArray(contractCompile("KittyCore.sol", "KittyCore"));
            Transaction tx3 = new Transaction(TransactionType.Contract, _acct2.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            tx3.sign(_acct2);
            deployContract(tx3,chain,repository,"KittyCore");

            dataBytes = hexToByteArray(contractCompile("BancorX.sol", "BancorX"));
            Transaction tx4 = new Transaction(TransactionType.Contract, _acct3.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            tx4.sign(_acct3);
            deployContract(tx4,chain,repository,"BancorX");

            dataBytes = hexToByteArray(contractCompile("BNB.sol", "BNB"));
            Transaction tx5 = new Transaction(TransactionType.Contract, _acct4.publicKey().pubKeyHash(), UInt160.fromBytes(ByteUtil.ZERO_BYTE_ARRAY_OF_LENGTH20), "",
                    Fixed8.fromDecimal(BigDecimal.ZERO), UInt256.assetId, 0L, new BinaryData(CryptoUtil.byteToList(dataBytes)),
                    new BinaryData(new ArrayList<>()), 0x01, null, ByteUtil.hexStringToBytes(Integer.toHexString(1)), ByteUtil.hexStringToBytes(Integer.toHexString(8000000)));

            tx5.sign(_acct4);
            deployContract(tx5,chain,repository,"BNB");

        } finally {
            chain.close();
        }
    }


}
