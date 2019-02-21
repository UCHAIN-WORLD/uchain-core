package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Witness {
    private String name;
    private String pubkey;
    private String privkey;

    public Witness(){

    }

    public Witness(String name, String pubkey, String privkey){
        this.name = name;
        this.pubkey = pubkey;
        this.privkey = privkey;
    }

    @Override
    public String toString() {
        return "Witness [name=" + name + ", pubkey=" + pubkey + ", privkey=" + privkey + "]";
    }
}
