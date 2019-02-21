package com.uchain.network.peer;

import com.uchain.network.NetworkUtil;
import com.uchain.network.PeerFeature;
import com.uchain.network.upnp.UPnP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerDatabaseImpl implements PeerDatabase {
    private static final Logger log = LoggerFactory.getLogger(PeerDatabaseImpl.class);

    private Map<InetSocketAddress, PeerInfo> whitelistPersistence = new HashMap<>();
    private Map<String, Long> blacklist = new HashMap<>();

    public PeerDatabaseImpl(String fileName) {
        //TODO maybe should write whilelist into file
    }

    @Override
    public void addOrUpdateKnownPeer(PeerInfo peerInfo) {
        log.trace("Add or Update known peer: " + peerInfo);

        if (peerInfo.getDeclaredAddress() != null) {
            String nodeNameOpt = peerInfo.getNodeName();
            NetworkUtil.ConnectionType connTypeOpt = peerInfo.getConnectionType();
            List<PeerFeature> features = peerInfo.getFeatures();

            InetSocketAddress address = peerInfo.getDeclaredAddress();
            PeerInfo dbPeerInfo = whitelistPersistence.get(address);
            if (dbPeerInfo != null) {
                if (nodeNameOpt.isEmpty()) {
                    nodeNameOpt = dbPeerInfo.getNodeName();
                }
                if (connTypeOpt == null) {
                    connTypeOpt = dbPeerInfo.getConnectionType();
                }
                if (features == null) {
                    features = dbPeerInfo.getFeatures();
                }
            }

            PeerInfo updatePeerInfo = new PeerInfo(peerInfo.getLastSeen(), peerInfo.getDeclaredAddress(), nodeNameOpt, connTypeOpt, features);
            whitelistPersistence.put(address, updatePeerInfo);
        }
    }

    @Override
    public Map<InetSocketAddress, PeerInfo> knowPeers() {
        return whitelistPersistence;
    }

    @Override
    public void blacklistPeer(InetSocketAddress address, long time) {
        log.warn("Black list peer:" + address.toString());
        whitelistPersistence.remove(address);
        if (!isBlacklisted(address)) {
            blacklist.put(address.getHostName(), time);
        }
    }

    @Override
    public List<String> blacklistedPeers() {
        return new ArrayList<>(blacklist.keySet());
    }

    @Override
    public boolean isBlacklisted(InetSocketAddress address) {
        synchronized (blacklist) {
            return blacklist.containsKey(address.getHostName());
        }

    }

    @Override
    public boolean remove(InetSocketAddress address) {
        log.trace("remove peer:" + address.toString());
        return whitelistPersistence.remove(address) != null;
    }

    @Override
    public boolean isEmpty() {
        return whitelistPersistence.isEmpty();
    }
}
