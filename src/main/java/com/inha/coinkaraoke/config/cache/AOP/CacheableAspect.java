package com.inha.coinkaraoke.config.cache.AOP;

import com.inha.coinkaraoke.config.cache.ReactorCacheManager;
import com.inha.coinkaraoke.exceptions.ThrowingSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Configuration
@ConditionalOnClass({ProceedingJoinPoint.class, MethodSignature.class,
        ReactiveRedisTemplate.class, RedisConnectionFactory.class,
        LettuceConnectionFactory.class, ReactiveRedisConnectionFactory.class})
@RequiredArgsConstructor
public class CacheableAspect {

    private final ReactorCacheManager reactorCacheManager;

    @SuppressWarnings("unchecked")
    @Around("execution(public * *(..)) && @annotation(Cacheable)")
    public Object getValue(ProceedingJoinPoint joinPoint) {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
        Type rawType = parameterizedType.getRawType();

        Object[] args = joinPoint.getArgs();

        ThrowingSupplier<Mono<byte[]>> retriever = () -> (Mono<byte[]>) joinPoint.proceed(args);


        if (rawType.equals(Mono.class)) {

            return reactorCacheManager
                    .findCachedMono(this.generateKey(args), retriever)
                    .log()
                    .doOnError(e -> log.error("Failed to processing mono cache. method: " + method.getName(), e));

        } else if (rawType.equals(Flux.class)) {

            throw new RuntimeException("Cache not supported for Flux type");
//            return reactorCacheManager
//                    .findCachedFlux(cacheName, this.generateKey(args), retriever)
//                    .doOnError(e -> log.error("Failed to processing flux cache. method: " + method.getName(), e));
        }
        else {

            throw new IllegalArgumentException("The return type is not Mono/Flux. Use Mono/Flux for return type. method: " + method.getName());
        }
    }

    private String generateKey(Object... objects) {

        return Arrays.stream(objects)
                .map(obj -> obj == null ? "" : obj.toString())
                .skip(1) // except Contract session id
                .collect(Collectors.joining(":"));
    }
}
