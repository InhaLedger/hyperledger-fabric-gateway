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
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.stereotype.Component;
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

        return Mono.fromCallable(request::pathVariables)
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .publishOn(Schedulers.boundedElastic())
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


        return Mono.fromCallable(request::pathVariables)
                .log()
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .publishOn(Schedulers.boundedElastic())
                .map(pathVariables -> {
                    String userId = pathVariables.get("userId");

                    Contract contract = gatewayUtils.getConnection(userId, CHAINCODE_NAME, CONTRACT_NAME);
                    // 처리는 진작에 완료되는데, 커넥션 타임아웃 될 때까지 기다리는 듯함.
                    return gatewayUtils.query(contract, "getAccount");
                })
                .flatMap(queryResult -> ServerResponse.ok().body(queryResult, byte[].class))
                .onErrorStop();
    }

}
