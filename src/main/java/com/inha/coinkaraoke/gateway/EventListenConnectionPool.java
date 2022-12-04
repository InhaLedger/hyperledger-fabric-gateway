package com.inha.coinkaraoke.gateway;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.exceptions.WalletProcessException;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link EventListenConnection} is read-only connection to observe block-creation events.
 */
@Component
@RequiredArgsConstructor
public class EventListenConnectionPool {

    private static final String DEFAULT_ORG = "Org1";
    private static final String ADMIN_USER = "admin";
    private final BlockEventChannel blockEventChannel;
    private final WalletManager walletManager;
    private final NetworkConfigStore networkConfigStore;
    private final Map<String,EventListenConnection> eventConnectionPool = new HashMap<>();

    public Boolean isEmpty() {

        return this.eventConnectionPool.isEmpty();
    }

    public Boolean isListeningTo(String channelName) {

        return this.eventConnectionPool.containsKey(channelName);
    }

    public void createFor(String channelName) {
        EventListenConnection connection = this.build(channelName);
        this.eventConnectionPool.put(channelName, connection);
    }

    private EventListenConnection build(String channelName) {

        Wallet orgWallet = walletManager.getWalletOf(DEFAULT_ORG);

        try {
            Network network = Gateway.createBuilder()
                    .identity(orgWallet, ADMIN_USER)
                    .discovery(true)
                    .networkConfig(networkConfigStore.getNetworkConfigPath(DEFAULT_ORG))
                    .connect()
                    .getNetwork(channelName);

            EventListenConnection connection = new EventListenConnection(network);
            connection.addBlockListener(blockEventChannel.getListener());

            return connection;

        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage(), e.getCause());
        }
    }
}
