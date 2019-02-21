package com.uchain.network.upnp;

import java.net.InetAddress;
import java.util.Map;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uchain.main.Settings;

public class UPnP {
    private static final Logger log = LoggerFactory.getLogger(UPnP.class);

    private Settings settings;

    public UPnP(Settings settings) {
        this.settings = settings;
    }

    public GatewayDevice getValidGateway() {
        GatewayDiscover discover = new GatewayDiscover();
        //  discover.setTimeout(timeOut);

        log.info("look up gateway device...");
        try {
            Map<InetAddress, GatewayDevice> gatewayMap = discover.discover();

            if (gatewayMap.isEmpty()) {
                log.debug("There are no UPnP gateway devices");
            } else {
                gatewayMap.forEach((inetAddress, gatewayDevice) -> {
                    log.debug("UPnP gateway device found on " + inetAddress.getHostAddress());
                });

                return discover.getValidGateway();  //return first valid gateway
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void upnpAddPort(Settings settings) throws Exception {
//        GatewayDiscover discover = new GatewayDiscover();
//        log.info("寻找UPnP网关设备...");
//        discover.discover();
//        GatewayDevice d = discover.getValidGateway();
//        if (null != d) {
//            log.info("找到的设备: " + d.getModelName() + " " + d.getModelDescription());
//        } else {
//            log.info("没有UPnP网关设备");
//            return;
//        }
//
//        InetAddress localAddress = d.getLocalAddress();
//        log.info("使用本地地址: " + localAddress);
//        String externalIPAddress = d.getExternalIPAddress();
//        log.info("外部IP地址是: " + externalIPAddress);
//        PortMappingEntry portMapping = new PortMappingEntry();
//
//        int port = Integer.parseInt(settings.getBindAddress().split(":")[1]);
//        if (d.getSpecificPortMappingEntry(port, "TCP", portMapping)) {
//            log.info("删除端口:" + port);
//            d.deletePortMapping(port, "TCP");
//        }
//        if (d.addPortMapping(port, port, localAddress.getHostAddress(), "TCP", "Uchain")) {
//            log.info("映射端口 [" + localAddress.getHostAddress() + "]:" + port);
//        } else {
//            log.info("无法映射端口: " + port);
//        }
//    }
}
