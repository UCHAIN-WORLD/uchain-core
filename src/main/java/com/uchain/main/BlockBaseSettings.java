package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockBaseSettings {
    String dir;
    boolean cacheEnabled;
    int cacheSize;

    public BlockBaseSettings(String dir, boolean cacheEnabled, int cacheSize){
        this.dir = dir;
        this.cacheEnabled = cacheEnabled;
        this.cacheSize = cacheSize;
    }
}
