package com.uchain.networkmanager.upnpsettings;

import java.net.InetSocketAddress;

public interface UPnPGateway {

    void addPort(int port);

    void deletePort(int port);

    InetSocketAddress getLocalAddressForExternalPort(int extrenalPort);

}
