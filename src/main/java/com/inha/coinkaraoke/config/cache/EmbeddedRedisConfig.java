package com.inha.coinkaraoke.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() {
        redisServer = new RedisServer(redisPort);
        redisServer.start();
        if (this.redisServer.isActive())
            log.info("Embedded Redis server starting... at localhost:{}", redisServer.ports());
        else {
            log.error("Embedded Redis server cannot start. Terminate application..");
            System.exit(0);
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (this.redisServer.isActive())
            redisServer.stop();
    }
}
