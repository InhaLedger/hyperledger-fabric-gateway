package com.inha.coinkaraoke.users;

import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;


@RequiredArgsConstructor
@Component
public class UserHandler {

    private static final String DEFAULT_ORG_NAME = "org1";

    private final HFCAClientManager hfcaClientManager;
    private final HFCAService hfcaService;
    private final WalletManager walletManager;

    @NonNull
    public Mono<ServerResponse> createUser(ServerRequest request) {

        String orgMspId = (String) request.attribute("orgMspId").orElse(DEFAULT_ORG_NAME);
        String userId = (String) request.attribute("userId")
                .orElseThrow(() -> new IllegalArgumentException("must specify userId."));

        Wallet orgWallet = walletManager.getWalletOf(orgMspId);

        HFCAClient caClient = hfcaClientManager.getCAClient(orgMspId);

        hfcaService.registerAndEnrollUser(userId, caClient, orgWallet, orgMspId);

        return ServerResponse.noContent().build();
    }

}
