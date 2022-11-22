package com.inha.coinkaraoke.handlers;

import com.inha.coinkaraoke.exceptions.BadRequestException;
import com.inha.coinkaraoke.gateway.GatewayUtils;
import com.inha.coinkaraoke.services.users.HFCAClientManager;
import com.inha.coinkaraoke.services.users.HFCAService;
import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway.Builder;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;


@Slf4j
@RequiredArgsConstructor
@Component
public class UserHandler {

    private static final String DEFAULT_ORG_NAME = "Org1";
    private static final String CHAINCODE_NAME = "chaincode";
    private static final String CONTRACT_NAME = "AccountContract";

    private final HFCAClientManager hfcaClientManager;
    private final HFCAService hfcaService;
    private final WalletManager walletManager;
    private final GatewayUtils gatewayUtils;

    @NonNull
    public Mono<ServerResponse> createUser(ServerRequest request) {

        return request.bodyToMono(UserRequest.class)
                .publishOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new BadRequestException("body is empty")))
                .flatMap(userRequest -> {
                    String orgId = userRequest.getOrgId();
                    String userId = userRequest.getUserId();

                    Wallet orgWallet = walletManager.getWalletOf(orgId);
                    HFCAClient caClient = hfcaClientManager.getCAClient(orgId);

                    hfcaService.registerAndEnrollUser(userId, caClient, orgWallet, orgId); // how to catch grpc errors?

                    return ServerResponse.noContent().build();
                })
                .onErrorStop();
    }

    @NonNull
    public Mono<ServerResponse> deleteUser(ServerRequest request) {

        return Mono.just(request.pathVariables())
                .publishOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .flatMap(pathVariables -> {
                    String orgId = pathVariables.getOrDefault("orgId", DEFAULT_ORG_NAME);
                    String userId = pathVariables.get("userId");

                    Wallet orgWallet = walletManager.getWalletOf(orgId);
                    HFCAClient caClient = hfcaClientManager.getCAClient(orgId);

                    hfcaService.revokeUser(userId, "", caClient, orgWallet, orgId); // how to catch grpc errors?

                    return ServerResponse.noContent().build();
                })
                .onErrorStop();
    }

    @NonNull
    public Mono<ServerResponse> getUserAccountInfo(ServerRequest request) {

        return Mono.just(request.pathVariables())
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .flatMap(pathVariables -> {
                    String orgId = pathVariables.getOrDefault("orgId", DEFAULT_ORG_NAME);
                    String userId = pathVariables.get("userId");

                    Builder builder = gatewayUtils.getBuilder(orgId, userId);
                    try (var gateway = builder.connect()) {
                        Contract contract = gatewayUtils.getContract(gateway, CHAINCODE_NAME, CONTRACT_NAME);
                        byte[] bytes = gatewayUtils.query(contract, "getAccount");

                        return ServerResponse.ok()
                                .body(BodyInserters.fromValue(bytes));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorStop();
    }

}
