package com.uchain.vm;
//

import com.uchain.core.Transaction;
import com.uchain.core.TransactionReceipt;
import com.uchain.core.TransactionType;
import com.uchain.crypto.*;
import com.uchain.solidity.CallTransaction;
import com.uchain.solidity.compiler.CompilationResult;
import com.uchain.solidity.compiler.SolidityCompiler;
import com.uchain.vm.program.invoke.ProgramInvokeFactory;
import com.uchain.vm.program.invoke.ProgramInvokeFactoryImpl;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.uchain.solidity.compiler.SolidityCompiler.Options.*;

public class CompilerTest {

    CommonConfig commonConfig;
    private ProgramInvokeFactory programInvokeFactory = new ProgramInvokeFactoryImpl();

    @Test
    public void simpleTest() throws Exception {
        String contract =
                "pragma solidity ^0.4.7;\n" +
                        "\n" +
                        "contract a {\n" +
                        "\n" +
                        "        mapping(address => string) private mailbox;\n" +
                        "\n" +
                        "        event Mailed(address from, string message);\n" +
                        "        event Read(address from, string message);\n" +
                        "\n" +
                        "}";

        SolidityCompiler.Result res = SolidityCompiler.compile(
                contract.getBytes(), true, ABI, BIN, INTERFACE, METADATA);
        System.out.println("Out: '" + res.output + "'");
        System.out.println("Err: '" + res.errors + "'");
        CompilationResult result = CompilationResult.parse(res.output);
        if (result.getContract("a") != null)
            System.out.println(result.getContract("a").bin);
        else
            Assert.fail();
        CompilationResult.ContractMetadata metadata = result.getContracts().iterator().next();
        if (metadata.bin == null || metadata.bin.isEmpty()) {
            throw new RuntimeException("Compilation failed, no binary returned:\n" + res.errors);
        } else {
            System.out.println(metadata.abi);
        }
        CompilationResult.ContractMetadata a = result.getContract("a");
        CallTransaction.Contract function = new CallTransaction.Contract(a.abi);
        System.out.println(function.functions[0].toString());
        //evm is wrong
        TransactionReceipt receipt = CreateContract(new byte[0], Hex.decode(metadata.bin));
    }

    protected TransactionReceipt CreateContract(byte[] receiveAddress, byte[] data) throws InterruptedException {

        //1.构造交易，创建合约特点是，没有to，只有from
        Long nonce = 0L;
        PrivateKey privkey = PrivateKey.fromWIF("L4CetTimu2rkYK1w3ZHtXdTjzRSHRKrzudquWowZhaLjQ4ts9AHG");
        String address = "UCgDinSCgoU5DaA6PfwQKzm5kXEVtkQjbKR";
        String assetId = "0000000000000000000000000000000000000000000000000000000000000000";
        String amount = "1";

        BigDecimal bigDecimal = new BigDecimal(amount);

        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < data.length; i++) {
            arrayList.add(i, data[i]);
        }
        Transaction tx = new Transaction(TransactionType.Contract,
                privkey.publicKey().pubKeyHash(),
                UInt160.Zero(),
                "",
                Fixed8.fromDecimal(bigDecimal),
                UInt256.fromBytes(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(assetId))),
                Long.valueOf(nonce),
                new BinaryData(arrayList),
                new BinaryData(new ArrayList<>()),
                0x01,
                null);
        tx.sign(privkey);

        //2.执行交易
       /* HashMapDB db = new HashMapDB();
        Repository repository = new RepositorySimple(db);
        TransactionExecutor executor = new TransactionExecutor(
                tx,repository);

        executor.init();
        executor.execute();
        executor.go();
//        executor.finalization();

        //3.拿到收据
        TransactionReceipt receipt = executor.getReceipt();
        System.out.println("==========================");
        System.out.println(receipt);*/
        return null;
    }
}
