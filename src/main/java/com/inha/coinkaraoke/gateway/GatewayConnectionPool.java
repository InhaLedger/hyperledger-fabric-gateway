package com.inha.coinkaraoke.gateway;

public interface GatewayConnectionPool {

    GatewayConnection findConnection(String userId);

    GatewayConnection addConnection(String userId, String orgId);

    GatewayConnection addConnection(String userId);
}
