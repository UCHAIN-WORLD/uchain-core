package com.uchain.networkmanager.peermanager;

import com.uchain.networkmanager.PeerFeature;
import lombok.AllArgsConstructor;


import java.net.InetSocketAddress;
import java.util.List;

import static com.uchain.networkmanager.NetworkUtil.ConnectionType;

@AllArgsConstructor
public class PeerInfo {
    private long lastSeen;
    private InetSocketAddress declaredAddress;
    private String nodeName;
    private ConnectionType connectionType;
    private List<PeerFeature> features;

    public boolean isReachablePeer() {
        return declaredAddress != null || isLocalAddress();
    }

    public boolean isLocalAddress() {
        //features.collectFirst { case LocalAddressPeerFeature(addr) => addr }
        //TODO
        return true;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public InetSocketAddress getDeclaredAddress() {
        return declaredAddress;
    }

    public void setDeclaredAddress(InetSocketAddress declaredAddress) {
        this.declaredAddress = declaredAddress;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public List<PeerFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<PeerFeature> features) {
        this.features = features;
    }
}
