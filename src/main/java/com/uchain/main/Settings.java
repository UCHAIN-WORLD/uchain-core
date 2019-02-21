package com.uchain.main;

import com.uchain.plugin.MongodbSetting;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import scala.concurrent.duration.FiniteDuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Settings {

    public static void getSysteProperties(String config) throws java.text.ParseException{
        Properties properties = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new FileReader(config));
            properties.load(bufferedReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SystemProperties systemProperties = new SystemProperties(Boolean.valueOf(properties.getProperty("vm.structured.trace")),
                Integer.parseInt(properties.getProperty("dump.block")),properties.getProperty("dump.style"),
                Boolean.valueOf(properties.getProperty("record.internal.transactions.data")),
                Boolean.valueOf(properties.getProperty("play.vm")),Boolean.valueOf(properties.getProperty("vm.structured.compressed")),
                properties.getProperty("solc.path"), false, false, false, false, false/*,
                properties.getProperty("crypto.providerName"),properties.getProperty("crypto.hash.alg256"),properties.getProperty("crypto.hash.alg512")*/);

        SystemProperties.setDefault(systemProperties);
    }

    public Settings(String config) throws java.text.ParseException {
        Properties properties = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(
                    new FileReader(config));
            properties.load(bufferedReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ResourceBundle resource = ResourceBundle.getBundle(config);
        this.nodeName = properties.getProperty("nodeName");
        this.bindAddress = properties.getProperty("bindAddress");
        this.knownPeers = properties.getProperty("knownPeers");
        this.agentName = properties.getProperty("agentName");
        this.maxPacketSize = properties.getProperty("maxPacketSize");
        this.localOnly = properties.getProperty("localOnly");
        this.appVersion = properties.getProperty("appVersion");
        this.maxConnections = properties.getProperty("maxConnections");
        this.connectionTimeout = properties.getProperty("connectionTimeout");
        this.upnpEnabled = Boolean.valueOf(properties.getProperty("upnpEnabled"));
        this.handshakeTimeout = properties.getProperty("handshakeTimeout");
        this.controllerTimeout = properties.getProperty("controllerTimeout");
        this.peerMaxTimeGap = properties.getProperty("peerMaxTimeGap");

        this.networkTimeProviderSettings = new NetworkTimeProviderSettings(properties.getProperty("server"),
                FiniteDuration.apply(Long.parseLong(properties.getProperty("updateEvery")), TimeUnit.MINUTES),
                FiniteDuration.apply(Long.parseLong(properties.getProperty("timeout")), TimeUnit.SECONDS));
        BlockBaseSettings blockBaseSetting = new BlockBaseSettings(properties.getProperty("chain_blockBase_dir"), Boolean.valueOf(properties.getProperty("chain_blockBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_blockBase_cacheSize")));
        DataBaseSettings dataBaseSetting = new DataBaseSettings(properties.getProperty("chain_dataBase_dir"), Boolean.valueOf(properties.getProperty("chain_dataBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_dataBase_cacheSize")));
        ForkBaseSettings forkBaseSettings = new ForkBaseSettings(properties.getProperty("chain_forkBase_dir"), Boolean.valueOf(properties.getProperty("chain_forkBase_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_forkBase_cacheSize")));
        this.transactionSummarySettings = new TransactionSummarySettings(properties.getProperty("chain_transactionReceipt_dir"), Boolean.valueOf(properties.getProperty("chain_transactionReceipt_cacheEnabled")),
                Integer.parseInt(properties.getProperty("chain_transactionReceipt_cacheSize")));


        this.chainSettings = new ChainSettings(blockBaseSetting, dataBaseSetting, forkBaseSettings,
                properties.getProperty("minerCoinFrom"), Double.valueOf(properties.getProperty("minerAward")), readTime(properties.getProperty("chain_genesis_timeStamp")),
                properties.getProperty("chain_genesis_publicKey"), properties.getProperty("chain_genesis_privateKey"),
                properties.getProperty("genesisCoinAirdrop"));

        String initialWitness = properties.getProperty("initialWitness");
        int produceInterval = Integer.parseInt(properties.getProperty("produceInterval"));
        int acceptableTimeError = Integer.parseInt(properties.getProperty("acceptableTimeError").trim());
        int producerRepetitions = Integer.parseInt(properties.getProperty("producerRepetitions").trim());
        this.consensusSettings = new ConsensusSettings(getWitness(initialWitness), produceInterval, acceptableTimeError, producerRepetitions,Integer.parseInt(properties.getProperty("runtimeParas.stopProcessTxTimeSlot").trim()));
        this.rpcServerSetting = new RPCServerSetting(properties.getProperty("rpcServerHost"), properties.getProperty("rpcServerPort"));
        this.mongodbSetting=new MongodbSetting(properties.getProperty("plugins_mongodb_uri"),Boolean.valueOf(properties.getProperty("plugins_mongodb_enabled")));
        this.config = new SystemProperties(Boolean.valueOf(properties.getProperty("vm.structured.trace")),Integer.parseInt(properties.getProperty("dump.block")),properties.getProperty("dump.style"),
                Boolean.valueOf(properties.getProperty("record.internal.transactions.data")),Boolean.valueOf(properties.getProperty("play.vm")),Boolean.valueOf(properties.getProperty("vm.structured.compressed")),
                properties.getProperty("solc.path"), false, false, false, false, false/*,
                properties.getProperty("crypto.providerName"),properties.getProperty("crypto.hash.alg256"),properties.getProperty("crypto.hash.alg512")*/);

        this.runtimeParasStopProcessTxTimeSlot = Integer.parseInt(properties.getProperty("runtimeParas.stopProcessTxTimeSlot").trim());
        this.runtimeParasTxAcceptGasLimit = new BigInteger(properties.getProperty("runtimeParas.txAcceptGasLimit").trim());
    }

    private static Long readTime(String time) throws java.text.ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        return fmt.parse(time).toInstant().toEpochMilli();
    }

    public void updatePort(String port){
        String defaultBindAddress = this.bindAddress;
        String defaultPort = defaultBindAddress.split(":")[1];
        this.bindAddress =  defaultBindAddress.replaceAll(":"+defaultPort, ":"+port);
    }

    private String nodeName;
    private String bindAddress;
    private String knownPeers;
    private String agentName;
    private String maxPacketSize;
    private String localOnly;
    private String appVersion;
    private String maxConnections;
    private String connectionTimeout;
    private boolean upnpEnabled;
    private String handshakeTimeout;
    private String controllerTimeout;
    private String peerMaxTimeGap;

    private ChainSettings chainSettings;
    private ConsensusSettings consensusSettings;
    private RPCServerSetting rpcServerSetting;
    private NetworkTimeProviderSettings networkTimeProviderSettings;
    private MongodbSetting mongodbSetting;
    private SystemProperties config;
    private TransactionSummarySettings transactionSummarySettings;
    private Integer runtimeParasStopProcessTxTimeSlot;
    private BigInteger runtimeParasTxAcceptGasLimit;

    private static List<Witness> getWitness(String json) {
        List<Witness> list = new ArrayList();
        try {
            JSONArray jsonObject = JSONArray.fromObject(json);
            for (Iterator<?> iterator = jsonObject.iterator(); iterator.hasNext(); ) {
                JSONObject job = (JSONObject) iterator.next();
                Iterator<?> it = job.keys();
                Witness witness = new Witness();
                int i = 0;
                while (it.hasNext()) {
                    if (i == 0) {
                        witness.setName((String) job.get(it.next()));
                    } else if (i == 1) {
                        witness.setPubkey((String) job.get(it.next()));
                    } else if (i == 2) {
                        witness.setPrivkey((String) job.get(it.next()));
                    }
                    i++;
                }
                list.add(witness);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
