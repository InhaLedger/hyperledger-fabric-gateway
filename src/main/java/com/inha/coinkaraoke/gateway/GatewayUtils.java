package com.inha.coinkaraoke.gateway;

import com.inha.coinkaraoke.exceptions.ChainCodeException;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class GatewayUtils {

    private final GatewayConnectionPool connectionPool;

    public Contract getConnection(String userId, String chaincodeId, String contractName) {

        GatewayConnection connection = connectionPool.findConnection(userId);
        if (connection == null) {
            connection = connectionPool.addConnection(userId);
        }
        connection.used();

        return connection.getContract(chaincodeId, contractName);
    }

//    @Cacheable
    public Mono<byte[]> query(Contract contract, String fxName, String... args) {

        return Mono.fromCallable(() -> contract.evaluateTransaction(fxName, args))
                .log()
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new ChainCodeException(e.getMessage(), e.getCause()));
    }

//    @CacheEvict
    public Mono<byte[]> submit(Contract contract, String fxName, String... args) {

        return Mono.fromCallable(() -> contract.submitTransaction(fxName, args))
                .log()
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> new ChainCodeException(e.getMessage(), e.getCause()));
    }
}














