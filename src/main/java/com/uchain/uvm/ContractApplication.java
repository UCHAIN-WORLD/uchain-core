package com.uchain.uvm;

import com.uchain.core.*;

public class ContractApplication {

//    @Autowired
//    EthJsonRpcImpl ethJsonRpc;
//
//    public String[] getComplier(){
//        return ethJsonRpc.eth_getCompilers();
//    }
//
//    public JsonRpc.CompilationResult compile_Solidity(String contract) throws Exception{
//        return ethJsonRpc.eth_compileSolidity(contract);
//    }
//
//    public UInt256 send_Transaction(JsonRpc.CallArguments args, Account account, PrivateKey privateKey){
//        if(args.data != null && args.data.startsWith("0x"))
//            args.data = args.data.substring(2);
//
//        final BigInteger valueBigInt = args.value != null ? hexToBigInteger(args.value) : BigInteger.ZERO;
//        final byte[] value = !valueBigInt.equals(BigInteger.ZERO) ? bigIntegerToBytes(valueBigInt) : EMPTY_BYTE_ARRAY;
//
//        byte[] toBytes = args.to != null ? hexToByteArray(args.to) : EMPTY_BYTE_ARRAY;
//
//        UInt160 toPubKeyHash = UInt160.fromBytes(toBytes);
//
//        final PublicKey publicKey = PublicKey.apply(CryptoUtil.fromHexString(args.from));
//
//        String assetId = "0000000000000000000000000000000000000000000000000000000000000000";
//        UInt256 asset = UInt256.fromBytes(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(assetId)));
//
//        final byte[] dataBytes = args.data != null ? hexToByteArray(args.data) : EMPTY_BYTE_ARRAY;
//
//        final Transaction tx = new Transaction(TransactionType.Contract, publicKey, toPubKeyHash, "",
//                Fixed8.One, asset, Long.valueOf(args.nonce), new BinaryData(CryptoUtil.byteToList(dataBytes)),
//                new BinaryData(new ArrayList<>()), 0x01, null);
//        tx.sign(privateKey);
//
//        return tx.id();
//    }

    public static TransactionReceipt CallContractExcutor(Transaction tx, BlockChain chain, Repository repository) throws Exception{


        return createCallTxAndExecute(tx, chain, repository);

    }

    protected static TransactionReceipt createCallTxAndExecute(Transaction tx,BlockChain chain, Repository repository) throws Exception {

        TransactionExecutor executor = new TransactionExecutor(tx,chain, repository, Long.MAX_VALUE);

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        return executor.getReceipt();
    }
}
