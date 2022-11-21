package com.inha.coinkaraoke.users;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.exceptions.BadRequestException;
import com.inha.coinkaraoke.exceptions.ChainCodeException;
import com.inha.coinkaraoke.users.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.*;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.NonNull;

import java.io.IOException;


@Slf4j
@RequiredArgsConstructor
@Component
public class UserHandler {

    private static final String DEFAULT_ORG_NAME = "Org1";
    private static final String CHANNEL_NAME = System.getenv().getOrDefault("CHANNEL_NAME", "mychannel");
    private static final String CHAINCODE_NAME = "chaincode";
    private static final String CONTRACT_NAME = "AccountContract";

    private final HFCAClientManager hfcaClientManager;
    private final HFCAService hfcaService;
    private final WalletManager walletManager;
    private final NetworkConfigStore networkConfigStore;

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
                .publishOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .flatMap(pathVariables -> {
                    String orgId = pathVariables.getOrDefault("orgId", DEFAULT_ORG_NAME);
                    String userId = pathVariables.get("userId");

                    Wallet orgWallet = walletManager.getWalletOf(orgId);

                    try {
                        Gateway.Builder builder = Gateway.createBuilder()
                                .identity(orgWallet, userId)
                                .discovery(true)
                                .networkConfig(networkConfigStore.getNetworkConfigPath(orgId));

                        try (var gateway = builder.connect()) {
                            Network channel = gateway.getNetwork(CHANNEL_NAME);
                            Contract contract = channel.getContract(CHAINCODE_NAME, CONTRACT_NAME);

                            byte[] bytes = contract.evaluateTransaction("getAccount");
                            return ServerResponse.ok()
                                    .body(BodyInserters.fromValue(bytes));

                        } catch (ContractException e) {
                            return Mono.error(new ChainCodeException(e.getMessage(), e.getCause()));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return Mono.error(e);
                    }
                })
                .onErrorStop();
    }

}
