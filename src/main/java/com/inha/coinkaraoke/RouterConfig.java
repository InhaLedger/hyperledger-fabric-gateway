package com.inha.coinkaraoke;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.inha.coinkaraoke.handlers.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;

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
