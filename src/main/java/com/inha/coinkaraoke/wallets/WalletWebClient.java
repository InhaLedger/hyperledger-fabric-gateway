package com.inha.coinkaraoke.wallets;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class WalletWebClient {

    private final WebClient client;

    public WalletWebClient(WebClient.Builder builder) {
        this.client = builder.baseUrl("http://localhost:8080").build();
    }

    public Mono<String> getMessage() {
        return this.client.get().uri("/wallet/hello").accept(MediaType.TEXT_PLAIN)
                .retrieve()
                .bodyToMono(String.class);
    }
}
