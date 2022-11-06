package com.inha.coinkaraoke.wallets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class WalletRouter {

    @Bean
    public RouterFunction<ServerResponse> routeWallet(WalletHandler walletHandler) {

        return RouterFunctions
                .route(RequestPredicates.GET("/wallet/hello").and(RequestPredicates.contentType(MediaType.TEXT_PLAIN))
                        , walletHandler::hello);

    }
}
