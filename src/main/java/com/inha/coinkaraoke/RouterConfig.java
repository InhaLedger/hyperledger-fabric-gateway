package com.inha.coinkaraoke;

import com.inha.coinkaraoke.handlers.BlockEventHandler;
import com.inha.coinkaraoke.handlers.CoinHandler;
import com.inha.coinkaraoke.handlers.ProposalHandler;
import com.inha.coinkaraoke.handlers.UserHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.resources;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@EnableWebFlux
@RequiredArgsConstructor
public class RouterConfig extends DelegatingWebFluxConfiguration {


    @Bean
    public RouterFunction<ServerResponse> userRouter(final UserHandler handler) {
        return route(POST("/users").and(accept(MediaType.APPLICATION_JSON)), handler::createUser)
                .andRoute(DELETE("/users/{orgId}/{userId}"), handler::deleteUser)
                .andRoute(GET("/users/{orgId}/{userId}/account"), handler::getUserAccountInfo);
    }

    @Bean
    public RouterFunction<ServerResponse> coinRouter(final CoinHandler handler) {
        return route(POST("/coins").and(accept(MediaType.APPLICATION_JSON)), handler::transfer)
                .andRoute(POST("/coins/new").and(accept(MediaType.APPLICATION_JSON)), handler::mint);
    }

    @Bean
    public RouterFunction<ServerResponse> proposalRouter(final ProposalHandler handler) {
        return route(POST("/proposals").and(accept(MediaType.APPLICATION_JSON)), handler::createProposal)
                .andRoute(GET("/proposals/{proposalId}/{type}"), handler::getProposal)
                .andRoute(POST("/proposals/{proposalId}/{type}/votes"), handler::voteToProposal)
                .andRoute(PUT("/proposals"), handler::callFinalize);
    }

    @Bean
    public RouterFunction<ServerResponse> blockRouter(final BlockEventHandler handler) {
        return route(GET("/blocks/{channel}"), handler::getBlockStreams);
    }

    @Bean
    public RouterFunction<ServerResponse> staticFilesRouter() {
        return resources("/css/**", new ClassPathResource("static/css/"))
                .and(resources("/js/**", new ClassPathResource("static/js/")));
    }
}
