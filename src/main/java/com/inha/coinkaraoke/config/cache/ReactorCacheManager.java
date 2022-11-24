package com.inha.coinkaraoke.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Objects;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Component
public class ReactorCacheManager {

    private final ReactiveRedisOperations<String, byte[]> redisTemplate;

    public Mono<byte[]> findCachedMono(String key, Supplier<Mono<byte[]>> retriever) {

        return CacheMono
                .lookup(k -> redisTemplate.opsForValue().get(key).map(Signal::next), key)
                .onCacheMissResume(()->Mono.defer(retriever))
                .andWriteWith((k, signal) -> Mono.fromRunnable(() -> {
                    if (!signal.isOnError()) {
                        redisTemplate.opsForValue().set(k, Objects.requireNonNull(signal.get())).ignoreElement();
                    }
                }));
    }

//    public <T> Flux<T> findCachedFlux(String key, Supplier<Flux<T>> retriever) {
//
//        return CacheFlux
//                .lookup(k -> {
//                    redisTemplate.opsForStream().
//                    List<T> result =  cache.get(k, List.class);
//                    return Mono.justOrEmpty(result)
//                            .flatMap(list -> Flux.fromIterable(list).materialize().collectList());
//                }, key)
//                .onCacheMissResume(Flux.defer(retriever))
//                .andWriteWith((k, signalList) -> Flux.fromIterable(signalList)
//                        .dematerialize()
//                        .collectList()
//                        .doOnNext(list -> {
//                            cache.put(k, list);
//                        })
//                        .then());
//    }
}
