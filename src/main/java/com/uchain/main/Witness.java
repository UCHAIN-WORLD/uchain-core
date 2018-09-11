package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Witness {
    private String name;
    private String pubkey;
    private String privkey;

    @Override
    public String toString() {
        return "Witness [name=" + name + ", pubkey=" + pubkey + ", privkey=" + privkey + "]";
    }
}
