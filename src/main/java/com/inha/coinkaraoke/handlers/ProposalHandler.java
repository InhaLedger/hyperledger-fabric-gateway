package com.inha.coinkaraoke.handlers;

import com.inha.coinkaraoke.exceptions.BadRequestException;
import com.inha.coinkaraoke.gateway.GatewayUtils;
import com.inha.coinkaraoke.services.proposals.ProposalRequest;
import com.inha.coinkaraoke.services.proposals.VoteRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProposalHandler {

    private static final String CHAINCODE_NAME = "chaincode";
    private static final String CONTRACT_NAME = "ProposalContract";
    private static final String ADMIN_USER = "admin";

    private final GatewayUtils gatewayUtils;

    @NonNull
    public Mono<ServerResponse> createProposal(ServerRequest request) {

        return request.bodyToMono(ProposalRequest.class)
                .switchIfEmpty(Mono.error(new BadRequestException()))
                .publishOn(Schedulers.boundedElastic())
                .map(proposalRequest -> {
                            String userId = proposalRequest.getUserId();
                            Long timestamp = proposalRequest.getTimestamp();
                            String type = proposalRequest.getType();

                            Contract connection = gatewayUtils.getConnection(userId, CHAINCODE_NAME,
                                    CONTRACT_NAME);
                            return gatewayUtils.submit(connection, "createProposal", type,
                                    String.valueOf(timestamp));
                        })
                .flatMap(submitResult->ServerResponse.ok().body(submitResult, byte[].class))
                .onErrorStop();
    }

    @NonNull
    public Mono<ServerResponse> getProposal(ServerRequest request) {

        return Mono.fromCallable(request::pathVariables)
                .switchIfEmpty(Mono.error(new BadRequestException("path variables are empty")))
                .publishOn(Schedulers.boundedElastic())
                .log()
                .map(pathVariables -> {
                    String proposalId = pathVariables.get("proposalId");
                    String type = pathVariables.get("type");
                    String userId = request.queryParam("userId")
                            .orElseThrow(() -> new BadRequestException("UserId is not given."));

                    Contract contract = gatewayUtils.getConnection(userId, CHAINCODE_NAME, CONTRACT_NAME);
                    return gatewayUtils.query(contract, "getProposal", proposalId, type);
                })
                .flatMap(queryResult -> ServerResponse.ok().body(queryResult, byte[].class))
                .onErrorStop();
    }

    @NonNull
    public Mono<ServerResponse> voteToProposal(ServerRequest request) {

        return request.bodyToMono(VoteRequest.class)
                .switchIfEmpty(Mono.error(new BadRequestException("request body is empty")))
                .publishOn(Schedulers.boundedElastic())
                .log()
                .map(voteRequest -> {
                    String amounts = voteRequest.getAmounts().toString();
                    String userId = voteRequest.getUserId();
                    String timestamp = voteRequest.getTimestamp().toString();
                    String proposalId = request.pathVariables().get("proposalId");
                    String type = request.pathVariables().get("type");

                    Contract contract = gatewayUtils.getConnection(userId, CHAINCODE_NAME, CONTRACT_NAME);
                    return gatewayUtils.query(contract, "getProposal", proposalId, type, amounts, timestamp);
                })
                .flatMap(queryResult -> ServerResponse.ok().body(queryResult, byte[].class))
                .onErrorStop();
    }
}
