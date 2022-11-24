package com.inha.coinkaraoke.handlers;

import com.inha.coinkaraoke.exceptions.BadRequestException;
import com.inha.coinkaraoke.gateway.GatewayUtils;
import com.inha.coinkaraoke.services.coins.dto.MintRequest;
import com.inha.coinkaraoke.services.coins.dto.TransferRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoinHandler {

    private static final String CHAINCODE_NAME = "chaincode";
    private static final String CONTRACT_NAME = "AccountContract";
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_ORG = "Org1";

    private final GatewayUtils gatewayUtils;

    @NonNull
    public Mono<ServerResponse> transfer(ServerRequest request) {

        AtomicReference<String> receiverId = new AtomicReference<>();
        AtomicReference<Double> amounts = new AtomicReference<>();

        return request.bodyToMono(TransferRequest.class)
                .switchIfEmpty(Mono.error(new BadRequestException()))
                .publishOn(Schedulers.boundedElastic())
                .map(transferRequest -> {
                    String senderId = transferRequest.getSenderId();
                    String senderOrg = transferRequest.getSenderOrg();
                    receiverId.set(transferRequest.getReceiverId());
                    amounts.set(transferRequest.getAmounts());
                    return gatewayUtils.getBuilder(senderOrg, senderId);
                })
                .map(builder -> Mono.using(builder::connect, gateway->{
                        Contract contract = gatewayUtils.getContract(gateway, CHAINCODE_NAME, CONTRACT_NAME);
                        return gatewayUtils.submit(contract, "transfer",
                                receiverId.get(), String.valueOf(new Date().getTime()), String.valueOf(amounts.get()));
                }, Gateway::close))
                .flatMap(submitResult->ServerResponse.ok().body(submitResult, byte[].class))
                .onErrorStop();
    }

    /**
     * This api must be strictly restricted from users. This api mints new coins to blockchain system.
     * Minting process can also be automated. Meanwhile, in this demo-application, we will not seriously
     * consider the security of the blockchain system for demonstration simplicity.
     * @param request JSON request which is the same shape with {@link MintRequest}
     */
    @NonNull
    public Mono<ServerResponse> mint(ServerRequest request) {

        AtomicReference<Double> amounts = new AtomicReference<>();

        return request.bodyToMono(MintRequest.class)
                .switchIfEmpty(Mono.error(new BadRequestException()))
                .publishOn(Schedulers.boundedElastic())
                .map(mintRequest -> {
                    amounts.set(mintRequest.getAmounts());
                    return gatewayUtils.getBuilder(ADMIN_ORG, ADMIN_USER);

                }).map(builder -> Mono.using(builder::connect, gateway -> {
                    Contract contract = gatewayUtils.getContract(gateway, CHAINCODE_NAME, CONTRACT_NAME);
                    return gatewayUtils.submit(contract, "mint", String.valueOf(amounts.get()));

                }, Gateway::close))
                .flatMap(submitResult -> ServerResponse.ok().body(submitResult, byte[].class))
                .onErrorStop();
    }

}
