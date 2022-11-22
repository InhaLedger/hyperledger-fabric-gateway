package com.inha.coinkaraoke;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.inha.coinkaraoke.handlers.CoinHandler;
import com.inha.coinkaraoke.handlers.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class RouterConfig extends DelegatingWebFluxConfiguration {

    private final UserHandler userHandler;
    private final CoinHandler coinHandler;

    @Bean
    public RouterFunction<ServerResponse> router() {
        return route()
                .add(this.userRouter())
                .add(this.coinRouter())
                .build();
    }


    private RouterFunction<ServerResponse> userRouter() {
        return route(POST("/users")
                        .and(accept(MediaType.APPLICATION_JSON)), userHandler::createUser)
                .andRoute(DELETE("/users/{orgId}/{userId}"), userHandler::deleteUser)
                .andRoute(GET("/users/{orgId}/{userId}/account"), userHandler::getUserAccountInfo);
    }

    private RouterFunction<ServerResponse> coinRouter() {
        return route(POST("/coins").and(accept(MediaType.APPLICATION_JSON)), coinHandler::transfer)
                .andRoute(POST("/coins/new").and(accept(MediaType.APPLICATION_JSON)), coinHandler::mint);
    }
}
