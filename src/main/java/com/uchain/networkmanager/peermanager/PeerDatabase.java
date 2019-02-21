package com.uchain.networkmanager.peermanager;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface PeerDatabase {

    boolean isEmpty();

    void addOrUpdateKnownPeer(PeerInfo peerInfo);

    Map<InetSocketAddress, PeerInfo> knowPeers();

    void blacklistPeer(InetSocketAddress peer, long time);

    List<String> blacklistedPeers();

    boolean isBlacklisted(InetSocketAddress address);

    boolean remove(InetSocketAddress address);
}
