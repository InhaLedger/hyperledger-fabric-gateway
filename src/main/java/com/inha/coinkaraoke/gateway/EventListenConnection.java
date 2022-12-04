package com.inha.coinkaraoke.gateway;

import org.hyperledger.fabric.gateway.Network;

/**
 * {@link EventListenConnection} is read-only connection to observe block-creation events.
 */
public class EventListenConnection extends GatewayConnection {
    public EventListenConnection(Network network) {
        super(network);
    }
}
