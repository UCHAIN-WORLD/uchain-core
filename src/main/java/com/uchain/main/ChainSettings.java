package com.uchain.main;

import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Setter
@Getter
public class ChainSettings {
    private BlockBaseSettings blockBaseSettings;
    private DataBaseSettings dataBaseSettings;
    private ForkBaseSettings forkBaseSettings;
    private String minerCoinFrom;
    private double minerAward;
    private Long chain_genesis_timeStamp;
    private String chain_genesis_publicKey;
    private String chain_genesis_privateKey;
//    private String coinToAddr;
    private List<CoinAirdrop> coinAirdrops;
    public ChainSettings(BlockBaseSettings blockBaseSettings, DataBaseSettings dataBaseSettings,
                         ForkBaseSettings forkBaseSettings, String minerCoinFrom, double minerAward,
                         Long chain_genesis_timeStamp, String chain_genesis_publicKey,
                         String chain_genesis_privateKey,String genesisCoinAirdrop) {
        this.blockBaseSettings = blockBaseSettings;
        this.dataBaseSettings = dataBaseSettings;
        this.forkBaseSettings = forkBaseSettings;
        this.minerCoinFrom = minerCoinFrom;
        this.minerAward = minerAward;
        this.chain_genesis_timeStamp = chain_genesis_timeStamp;
        this.chain_genesis_publicKey = chain_genesis_publicKey;
        this.chain_genesis_privateKey = chain_genesis_privateKey;
//        this.coinToAddr = coinToAddr;
        this.coinAirdrops = getCoinAirdropsBySettings(genesisCoinAirdrop);
    }

    private List<CoinAirdrop> getCoinAirdropsBySettings(String genesisCoinAirdrop){
        List<CoinAirdrop> list = new ArrayList();
        try {
            JSONArray jsonObject = JSONArray.fromObject(genesisCoinAirdrop);
            for (Iterator<?> iterator = jsonObject.iterator(); iterator.hasNext(); ) {
                JSONObject job = (JSONObject) iterator.next();
                Iterator<?> it = job.keys();
                CoinAirdrop coinAirdrop = new CoinAirdrop();
                int i = 0;
                while (it.hasNext()) {
                    if (i == 0) {
                        coinAirdrop.setAddr((String) job.get(it.next()));
                    } else if (i == 1) {
                        String balance = (String) job.get(it.next());
                        coinAirdrop.setCoins(Double.parseDouble(balance));
                    }
                    i++;
                }
                list.add(coinAirdrop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
