package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RPCServerSetting {
    String rpcServerHost;
    String rpcServerPort;

    RPCServerSetting(String rpcServerHost, String rpcServerPort){
        this.rpcServerHost = rpcServerHost;
        this.rpcServerPort = rpcServerPort;
    }
}
