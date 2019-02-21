package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoinAirdrop {
    private String addr;
    private Double coins;

    public CoinAirdrop(){}

    public CoinAirdrop(String addr, Double coins){
        this.addr = addr;
        this.coins = coins;
    }
}
