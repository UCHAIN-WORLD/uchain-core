package com.uchain.config.blockchain;

public class MainNetConfig extends BaseNetConfig {
    public static final MainNetConfig INSTANCE = new MainNetConfig();

    public MainNetConfig() {
        add(0, new FrontierConfig());
        add(1_150_000, new HomesteadConfig());
        add(1_920_000, new DaoHFConfig());
        add(2_463_000, new Eip150HFConfig(new DaoHFConfig()));
        add(2_675_000, new Eip160HFConfig(new DaoHFConfig()));
        add(4_370_000, new ByzantiumConfig(new DaoHFConfig()));
    }
}
