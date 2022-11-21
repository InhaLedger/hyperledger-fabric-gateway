package com.inha.coinkaraoke;

import com.inha.coinkaraoke.users.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class RouterConfig extends DelegatingWebFluxConfiguration {

    private final UserHandler userHandler;

    @Bean
    public RouterFunction<?> userRouter() {
        return route(POST("/users")
                        .and(accept(MediaType.APPLICATION_JSON)), userHandler::createUser)
                .andRoute(DELETE("/users/{orgId}/{userId}"), userHandler::deleteUser)
                .andRoute(GET("/users/{orgId}/{userId}/account"), userHandler::getUserAccountInfo);
    }
}
