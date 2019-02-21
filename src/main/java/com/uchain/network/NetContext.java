package com.uchain.network;

import com.uchain.network.message.MessageSpec;
import com.uchain.network.upnp.UPnPGateway;
import com.uchain.util.NetworkTimeProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NetContext {

    private List<MessageSpec> messageSpecs;
    private UPnPGateway uPnPGateway;
    private NetworkTimeProvider networkTimeProvider;
    InetSocketAddress externalNodeAddress;

}
