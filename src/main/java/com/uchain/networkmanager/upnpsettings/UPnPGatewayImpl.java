package com.uchain.networkmanager.upnpsettings;

import lombok.Getter;
import lombok.Setter;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Getter
@Setter
public class UPnPGatewayImpl implements UPnPGateway {

    private static final Logger log = LoggerFactory.getLogger(UPnPGatewayImpl.class);

    private InetAddress localAddress;
    private InetAddress externalAddress;
    private GatewayDevice gateway;

    public UPnPGatewayImpl(GatewayDevice gateway) {
        this.gateway = gateway;
        this.localAddress = gateway.getLocalAddress();
        try {
            this.externalAddress = InetAddress.getByName(gateway.getExternalIPAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addPort(int port) {
        try {
            if (gateway.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "UCHAIN")) {
                log.debug("Mapped port [" + externalAddress.getHostAddress() + "]:" + port);
            } else {
                log.debug("Unable to map port " + port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePort(int port) {
        try {
            if (gateway.deletePortMapping(port, "TCP")) {
                log.debug("Mapping deleted for port " + port);
            } else {
                log.debug("Unable to delete mapping for port " + port);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InetSocketAddress getLocalAddressForExternalPort(int externalPort) {
        try {
            PortMappingEntry entry = new PortMappingEntry();
            if (gateway.getSpecificPortMappingEntry(externalPort, "TCP", entry)) {
                String host = entry.getInternalClient();
                return new InetSocketAddress(InetAddress.getByName(host), entry.getInternalPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
