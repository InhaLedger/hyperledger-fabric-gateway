package com.inha.coinkaraoke;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.inha.coinkaraoke.handlers.CoinHandler;
import com.inha.coinkaraoke.handlers.ProposalHandler;
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

    @Bean
    public RouterFunction<ServerResponse> router(
            final UserHandler userHandler,
            final CoinHandler coinHandler,
            final ProposalHandler proposalHandler
    ) {
        return route()
                .add(this.userRouter(userHandler))
                .add(this.coinRouter(coinHandler))
                .add(this.proposalRouter(proposalHandler))
                .build();
    }


    private RouterFunction<ServerResponse> userRouter(final UserHandler handler) {
        return route(POST("/users")
                        .and(accept(MediaType.APPLICATION_JSON)), handler::createUser)
                .andRoute(DELETE("/users/{orgId}/{userId}"), handler::deleteUser)
                .andRoute(GET("/users/{orgId}/{userId}/account"), handler::getUserAccountInfo);
    }

    private RouterFunction<ServerResponse> coinRouter(final CoinHandler handler) {
        return route(POST("/coins").and(accept(MediaType.APPLICATION_JSON)), handler::transfer)
                .andRoute(POST("/coins/new").and(accept(MediaType.APPLICATION_JSON)), handler::mint);
    }

    private RouterFunction<ServerResponse> proposalRouter(final ProposalHandler handler) {
        return route(POST("/proposal").and(accept(MediaType.APPLICATION_JSON)), handler::createProposal);
    }
}
