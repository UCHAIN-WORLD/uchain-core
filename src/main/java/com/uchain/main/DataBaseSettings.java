package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataBaseSettings {
    String dir;
    boolean cacheEnabled;
    int cacheSize;

    public DataBaseSettings(String dir, boolean cacheEnabled, int cacheSize){
        this.dir = dir;
        this.cacheEnabled = cacheEnabled;
        this.cacheSize = cacheSize;
    }
}
