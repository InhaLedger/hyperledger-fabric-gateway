package com.inha.coinkaraoke.config.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Primary
    @Bean
    public ReactiveRedisOperations<String, byte[]> redisOperations(ReactiveRedisConnectionFactory factory) {
        RedisSerializer<String> serializer = new StringRedisSerializer();
        RedisSerializationContext<String, byte[]> context = RedisSerializationContext
                .<String, byte[]>newSerializationContext()
                .key(serializer)
                .hashKey(serializer)
                .value(RedisSerializationContext.byteArray().getValueSerializationPair())
                .hashValue(RedisSerializationContext.byteArray().getHashValueSerializationPair())
                .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
