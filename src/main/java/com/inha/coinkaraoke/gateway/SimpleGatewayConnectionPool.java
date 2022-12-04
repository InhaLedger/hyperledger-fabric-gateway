package com.inha.coinkaraoke.gateway;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.exceptions.WalletProcessException;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SimpleGatewayConnectionPool implements GatewayConnectionPool {

    @Value("${fabric.network.channel}")
    private String CHANNEL_NAME; // static 으로 하면 설정파일에서 못 읽고, 하드코딩 해야함.
    private static final String DEFAULT_ORG = "Org1";
    private static final Integer MAX_CONNECTION = 30;
    private final Map<String, GatewayConnection> connections = new ConcurrentHashMap<>(); // Key is UserId

    private final WalletManager walletManager;
    private final NetworkConfigStore networkConfigStore;


    @Override
    public GatewayConnection findConnection(String userId) {

        if (this.connections.containsKey(userId)) {
            GatewayConnection network = this.connections.get(userId);

            if (network.isShutdown()) {
                this.connections.remove(userId);
                return null;

            } else {
                return network;
            }
        }
        return null;
    }


    @Override
    public GatewayConnection addConnection(String userId, String orgId) {

        GatewayConnection connection = this.buildConnection(userId, orgId);

        if (this.connections.size() == MAX_CONNECTION) {
            this.removeLastUsedConnection();
        }
        this.connections.put(userId, connection);

        return connection;
    }

    @Override
    public GatewayConnection addConnection(String userId) {

        return this.addConnection(userId, DEFAULT_ORG);
    }

    private GatewayConnection buildConnection(String userId, String orgId) {
        Wallet orgWallet = walletManager.getWalletOf(orgId);

        try {
            Network network = Gateway.createBuilder()
                    .identity(orgWallet, userId)
                    .discovery(true)
                    .networkConfig(networkConfigStore.getNetworkConfigPath(orgId))
                    .connect()
                    .getNetwork(CHANNEL_NAME);

            return new GatewayConnection(network);

        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage(), e.getCause());
        }
    }

    private void removeLastUsedConnection() {

        Long timestamp = Long.MAX_VALUE;
        GatewayConnection target = null;
        String targetKey = "";
        for (var item : this.connections.entrySet()) {
            if (timestamp > item.getValue().getTimestamp()) {
                target = item.getValue();
                timestamp = item.getValue().getTimestamp();
                targetKey = item.getKey();
            }
        }

        assert target != null;
        target.close();
        this.connections.remove(targetKey);
    }

    @PreDestroy
    protected void clear() {
        for (var item : this.connections.entrySet()) {
            if(!item.getValue().isShutdown())
                item.getValue().close();
        }
    }
}
