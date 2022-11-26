package com.inha.coinkaraoke.config;

import org.apache.commons.io.FileUtils;
import org.hyperledger.fabric.gateway.impl.identity.FileSystemWalletStore;
import org.hyperledger.fabric.gateway.spi.WalletStore;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.DelegatingWebFluxConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

@Configuration
public class BeanConfig extends DelegatingWebFluxConfiguration {

    @Value("${fabric.network.config.Org1}")
    private String org1NetworkConfig;

    @Bean
    public WalletStore getWalletStore() throws IOException {

        return new FileSystemWalletStore(Path.of("wallets"));
    }

    @Bean
    public NetworkConfigStore networkConfigStore()
            throws IOException, NetworkConfigurationException {

        return new NetworkConfigStore.Builder()
                .addConfigFile("Org1", Path.of(FileUtils.getUserDirectoryPath(), org1NetworkConfig).toString())
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("*"));
        corsConfig.setMaxAge(8000L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
