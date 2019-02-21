package com.uchain.network.upnp;

import java.net.InetSocketAddress;

public interface UPnPGateway {

    void addPort(int port);

    void deletePort(int port);

    InetSocketAddress getLocalAddressForExternalPort(int extrenalPort);

}
