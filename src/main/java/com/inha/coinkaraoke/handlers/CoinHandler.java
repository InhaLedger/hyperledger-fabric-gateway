package com.inha.coinkaraoke.handlers;

import com.inha.coinkaraoke.exceptions.BadRequestException;
import com.inha.coinkaraoke.gateway.GatewayUtils;
import com.inha.coinkaraoke.services.coins.dto.TransferRequest;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway.Builder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoinHandler {

    private static final String CHAINCODE_NAME = "chaincode";
    private static final String CONTRACT_NAME = "AccountContract";

    private final GatewayUtils gatewayUtils;

    @NonNull
    public Mono<ServerResponse> transfer(ServerRequest request) {

        return request.bodyToMono(TransferRequest.class)
                .publishOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.error(new BadRequestException()))
                .flatMap(transferRequest -> {

                    String senderId = transferRequest.getSenderId();
                    String receiverId = transferRequest.getReceiverId();
                    String senderOrg = transferRequest.getSenderOrg();

                    Builder builder = gatewayUtils.getBuilder(senderOrg, senderId);
                    byte[] bytes;
                    try (var gateway = builder.connect()) {
                        Contract contract = gatewayUtils.getContract(gateway, CHAINCODE_NAME, CONTRACT_NAME);
                        bytes = gatewayUtils.submit(contract, "transfer",
                                receiverId, String.valueOf(new Date().getTime()), transferRequest.getAmounts());
                    }
                    return ServerResponse.ok()
                            .body(BodyInserters.fromValue(bytes));
                })
                .onErrorStop();
    }

}
