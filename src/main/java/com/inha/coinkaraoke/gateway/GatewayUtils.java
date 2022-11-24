package com.inha.coinkaraoke.gateway;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.config.cache.AOP.CacheEvict;
import com.inha.coinkaraoke.config.cache.AOP.Cacheable;
import com.inha.coinkaraoke.exceptions.ChainCodeException;
import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.exceptions.WalletProcessException;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.sdk.Channel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class GatewayUtils {

    @Value("${fabric.network.channel}")
    private String CHANNEL_NAME; // static 으로 하면 설정파일에서 못 읽고, 하드코딩 해야함.
    private final WalletManager walletManager;
    private final NetworkConfigStore networkConfigStore;


    public Gateway.Builder getBuilder(String orgId, String userId) {
        Wallet orgWallet = walletManager.getWalletOf(orgId);

        try {
            return Gateway.createBuilder()
                    .identity(orgWallet, userId)
                    .discovery(true)
                    .networkConfig(networkConfigStore.getNetworkConfigPath(orgId));
        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage(), e.getCause());
        }
    }

    public Contract getContract(Gateway gateway, String chaincode, String contract) {
        return gateway
                .getNetwork(CHANNEL_NAME)
                .getContract(chaincode, contract);
    }

    @Cacheable
    public Mono<byte[]> query(Contract contract, String fxName, String... args) {

        return Mono.fromCallable(() -> contract.evaluateTransaction(fxName, args))
                .log()
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new ChainCodeException(e.getMessage(), e.getCause()));
    }

    @CacheEvict
    public Mono<byte[]> submit(Contract contract, String fxName, String... args) {

        return Mono.fromCallable(() -> contract.submitTransaction(fxName, args))
                .log()
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new ChainCodeException(e.getMessage(), e.getCause()));
    }

    public void shutdown(Gateway gateway) {
        Channel channel = gateway.getNetwork(CHANNEL_NAME).getChannel();
        if (!channel.isShutdown())
            channel.shutdown(true);
    }
}














