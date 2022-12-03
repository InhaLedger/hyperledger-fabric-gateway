package com.inha.coinkaraoke.gateway;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.sdk.BlockEvent;

import java.util.Date;
import java.util.function.Consumer;

public class GatewayConnection {

    private final Network network;
    private final Long createdTimestamp;
    private Long timestamp;

    public GatewayConnection(Network network) {
        this.network = network;
        this.createdTimestamp = new Date().getTime();
        this.timestamp = createdTimestamp;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void used() {
        this.timestamp = new Date().getTime();
    }

    public Contract getContract(String chaincodeId, String contractName) {
        return this.network.getContract(chaincodeId, contractName);
    }

    public Boolean isShutdown() {
        return this.network.getChannel().isShutdown();
    }

    public void close() {
        this.network.getGateway().close();
    }

    public Consumer<BlockEvent> addBlockListener(Consumer<BlockEvent> listener) {
        return this.network.addBlockListener(listener);
    }
}
