package com.inha.coinkaraoke.gateway;


import java.util.function.Consumer;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.publisher.Sinks.Many;
import reactor.util.concurrent.Queues;

@Component
public class BlockEventChannel {

    private final Many<BlockEvent> eventPublisher;

    public BlockEventChannel() {
        eventPublisher = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);
    }

    public Consumer<BlockEvent> getListener() {
        return event -> this.eventPublisher.emitNext(event, EmitFailureHandler.FAIL_FAST);
    }

    public Flux<BlockEvent> toFlux() {
        return this.eventPublisher.asFlux();
    }

}
