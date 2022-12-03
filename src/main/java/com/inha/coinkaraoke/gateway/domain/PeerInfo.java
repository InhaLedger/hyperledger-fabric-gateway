package com.inha.coinkaraoke.gateway.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PeerInfo {

    private final String name;
    private final String url;

    public static PeerInfo from(org.hyperledger.fabric.sdk.Peer peer) {

        return new PeerInfo(peer.getName(), peer.getUrl());
    }

    @JsonCreator
    private PeerInfo(
            @JsonProperty("name") String name,
            @JsonProperty("url") String url) {

        this.name = name;
        this.url = url;
    }
}
