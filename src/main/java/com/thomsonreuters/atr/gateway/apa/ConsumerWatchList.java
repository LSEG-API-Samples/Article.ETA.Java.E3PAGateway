package com.thomsonreuters.atr.gateway.apa;

import com.thomsonreuters.upa.framework.events.ReliableProducerStreamInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Copyright (C) 2017 Thomson Reuters. All rights reserved.
 * Simple map of nodes to stream infos
 */
class ConsumerWatchList {

    private final ConcurrentHashMap<String, ReliableProducerStreamInfo> watchList = new ConcurrentHashMap<>();

    void addNode(String name, ReliableProducerStreamInfo streamInfo) {
        watchList.put(name, streamInfo);
    }

    Optional<ReliableProducerStreamInfo> getNode(String name) {
        return Optional.ofNullable(watchList.get(name));
    }

    void deleteNode(String name) {
        watchList.remove(name);
    }

    Collection<ReliableProducerStreamInfo> getEntries() {
        return watchList.values();
    }
}
