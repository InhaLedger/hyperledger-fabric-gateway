package com.inha.coinkaraoke;

import com.inha.coinkaraoke.networks.NetworkConfigStore;
import java.io.IOException;
import java.nio.file.Path;
import org.hyperledger.fabric.gateway.impl.identity.FileSystemWalletStore;
import org.hyperledger.fabric.gateway.spi.WalletStore;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {


    @Bean
    public WalletStore getWalletStore() throws IOException {

        return new FileSystemWalletStore(Path.of("wallets"));
    }

    @Bean
    public NetworkConfigStore networkConfigStore()
            throws IOException, NetworkConfigurationException {

        return new NetworkConfigStore.Builder()
                .setClassPath()
                .addConfigFile("org1", "classpath:connection-org1.yaml")
                .build();
    }
}
