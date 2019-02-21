package com.uchain.plugin;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MongodbSetting {
    private String plugins_mongodb_uri;
    private boolean plugins_mongodb_enabled;

    public MongodbSetting(String plugins_mongodb_uri,boolean plugins_mongodb_enabled){
        this.plugins_mongodb_uri=plugins_mongodb_uri;
        this.plugins_mongodb_enabled=plugins_mongodb_enabled;
    }
}
