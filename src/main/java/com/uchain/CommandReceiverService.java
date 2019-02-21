package com.uchain;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.datastore.BlockBase;
import com.uchain.core.producer.SendRawTransaction;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.UInt256;
import com.uchain.network.message.BlockMessageImpl;
import lombok.val;

import java.io.IOException;
import java.util.*;

public class CommandReceiverService {

    static final String[] cmdSupport= {"getaccount"
            ,"getblocks"
            ,"sendrawtransaction"
            ,"newaddr"
            ,"getblock"
            ,"gettx"
            ,"importprivkey"
            ,"walletinfo"
            ,"getblockcount"
            ,"send"
            ,"address"
            ,"amount"
            ,"to"
            ,"id"
            ,"h"
            ,"data"
            ,"-address"
            ,"-amount"
            ,"-to"
            ,"-id"
            ,"-h"
            ,"-data"
    };
    public static int commandCheck(String query){
        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        Set<String> test = new TreeSet<>();

        for(CommandSuffix obj : commandSuffixes){
            if(!Arrays.asList(cmdSupport).contains(obj.getSuffixParam())){
                return 400;
            }
            test.add(obj.getSuffixParam());
        }

        if(commandSuffixes.size() >  test.size()){
            return 400;
        }

        return 200;
    }
    static public String getBlocks(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
        BlockBase blockBase = chain.getBlockBase();
        List<Map.Entry<byte[], byte[]>> entryList= blockBase.getBlockStore().getDb().scan();
        val blockNum = chain.getHeight();
        val blocks = new ArrayList<Block>(blockNum);
        try {
            for(int i=blockNum-1; i> 0; i--){
                blocks.add(chain.getBlock(i));
            }

            return Serializabler.JsonMapperTo(blocks);
        }
        catch (Throwable e){
            e.printStackTrace();
            return "";
        }

    }

    public static BlockMessageImpl.RPCCommandMessage getBlock(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
            ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
            if(commandSuffixes.size() ==1){
                val suffixParam = commandSuffixes.get(0).getSuffixParam();
                val suffixValue = commandSuffixes.get(0).getSuffixValue();
                if(suffixParam.contains("id")) return new BlockMessageImpl.GetBlockByIdCmd(UInt256.parse(suffixValue));
                if(suffixParam.contains("h")) return new BlockMessageImpl.GetBlockByHeightCmd(Integer.valueOf(suffixValue));
            }
            return null;
    }

    public static String getAccount(String query, ActorRef nodeActor, ActorRef producerActor){
//        try {
        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        if(commandSuffixes.size() ==1){
            val suffixParam = commandSuffixes.get(0).getSuffixParam();
            val suffixValue = commandSuffixes.get(0).getSuffixValue();
            return suffixValue;
        }
        return "";
//        }
//        catch (IOException e){
//            e.printStackTrace();
//            return "";
//        }
    }

    static public SendRawTransaction sendRawTransaction(String query, ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){

        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        BinaryData txRawData = CryptoUtil.fromHexString(commandSuffixes.get(0).getSuffixValue());
/*
        BinaryData privKeyBin = CryptoUtil.fromHexString(commandSuffixes.get(0).getSuffixValue());

        val privKey = PrivateKey.apply(privKeyBin);

        val toAddress = commandSuffixes.get(1).getSuffixValue();
        val assetId = commandSuffixes.get(2).getSuffixValue();
        val amount = new BigDecimal(commandSuffixes.get(3).getSuffixValue());

        val nonce = Integer.valueOf(commandSuffixes.get(4).getSuffixValue());

        val keyHash = PublicKeyHash.fromAddress(toAddress);

        val tx = new Transaction(TransactionType.Transfer, privKey.publicKey(), PublicKeyHash.fromAddress(toAddress),
                "", Fixed8.fromDecimal(amount), UInt256.fromBytes(CryptoUtil.binaryData2array(CryptoUtil.fromHexString(assetId))),
                (long)nonce, new BinaryData(new ArrayList<>()),  new BinaryData(new ArrayList<>()), 0x01, null);
        try {
            tx.sign(privKey);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        byte[] txBytes = Serializabler.toBytes(tx);
        Byte[] txBYTES = new Byte[txBytes.length];
        for (int i = 0; i < txBytes.length; i++){
            txBYTES[i] = txBytes[i];
        }
        List<Byte> txList = Arrays.asList(txBYTES);
        val txRawData = new BinaryData(txList);
        val rawTx = "{\"rawTx\":\""  + CryptoUtil.toHexString(txRawData)  + "\"}";*/
        return new SendRawTransaction(txRawData,chain);
    }

    public static ArrayList<CommandSuffix> parseCommandSuffixes(String height){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            JavaType javaType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, CommandSuffix.class);
            return mapper.readValue(height, javaType);
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<CommandSuffix>();
        }
    }

    public static String getTransactionHash(String query){
        ArrayList<CommandSuffix> commandSuffixes = parseCommandSuffixes(query);
        if(commandSuffixes.size() ==1){
            val suffixParam = commandSuffixes.get(0).getSuffixParam();
            val suffixValue = commandSuffixes.get(0).getSuffixValue();
            return suffixValue;
        }
        return null;
    }
}
