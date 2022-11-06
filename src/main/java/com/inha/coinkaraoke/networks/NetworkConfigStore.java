package com.inha.coinkaraoke.networks;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

@Component
public class NetworkConfigStore {

    private Map<String, NetworkConfig> store;

    private NetworkConfigStore() {
        this.store = Map.of();
    }

    private void addNetworkConfig(String key, NetworkConfig config) {
        this.store.put(key, config);
    }

    public static class Builder {

        private Map<String, String> configFiles = Map.of();
        private Boolean classpath = false;

        public Builder addConfigFile(String key, String path) {
            this.configFiles.put(key, path);
            return this;
        }

        public Builder setClassPath() {
            this.classpath = true;
            return this;
        }


        public NetworkConfigStore build() throws IOException, NetworkConfigurationException {

            NetworkConfigStore configStore = new NetworkConfigStore();

            for (var entry : this.configFiles.entrySet()) {

                File config;
                if (this.classpath) {
                    config = ResourceUtils.getFile(entry.getValue());
                } else {
                    config = new File(entry.getValue());
                }

                NetworkConfig networkConfig;
                if (isYaml(config))
                    networkConfig = NetworkConfig.fromYamlFile(config);
                else if (isJson(config))
                    networkConfig = NetworkConfig.fromJsonFile(config);
                else
                    throw new IllegalArgumentException("cannot read file format. please check the filename has an extension.");

                configStore.addNetworkConfig(entry.getKey(), networkConfig);
            }

            return configStore;
        }

        private boolean isJson(File config) {
            return FilenameUtils.getExtension(config.getName()).equals("json");
        }

        private boolean isYaml(File config) {
            return FilenameUtils.getExtension(config.getName()).equals("yaml");
        }
    }
}
