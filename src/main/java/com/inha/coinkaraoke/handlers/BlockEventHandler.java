package com.inha.coinkaraoke.handlers;

import com.inha.coinkaraoke.gateway.BlockEventChannel;
import com.inha.coinkaraoke.gateway.domain.BlockInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.thymeleaf.spring5.context.webflux.IReactiveDataDriverContextVariable;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlockEventHandler {

    private final BlockEventChannel blockEventChannel;

    public Mono<ServerResponse> getBlockStreams(ServerRequest request) {

        Flux<BlockInfo> blockFlux = blockEventChannel.toFlux()
                .log()
                .publishOn(Schedulers.boundedElastic())
                .map(BlockInfo::from);

        final IReactiveDataDriverContextVariable dataDriver =
                new ReactiveDataDriverContextVariable(blockFlux, 1, 1);

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("blocks", Collections.singletonMap("data", dataDriver));
    }
}
