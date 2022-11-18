package com.inha.coinkaraoke.networks;

import org.apache.commons.io.FilenameUtils;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkConfigStore {

    private final Map<String, NetworkConfig> store;

    private NetworkConfigStore() {
        this.store = new HashMap<>();
    }

    public void addNetworkConfig(String key, NetworkConfig config) {
        this.store.put(key, config);
    }

    public NetworkConfig getNetworkConfig(String key) {

        return this.store.get(key);
    }
    public static class Builder {

        private final Map<String, String> configFiles = new HashMap<>();
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
