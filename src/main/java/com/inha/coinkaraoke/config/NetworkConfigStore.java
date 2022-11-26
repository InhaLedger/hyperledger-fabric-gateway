package com.inha.coinkaraoke.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NetworkConfigStore {

    private final Map<String, NetworkConfig> store;
    private final Map<String, Path> pathStore;

    private NetworkConfigStore() {
        this.store = new HashMap<>();
        this.pathStore = new HashMap<>();
    }

    public void addNetworkConfig(String key, File file) throws IOException, NetworkConfigurationException {

        NetworkConfig networkConfig;
        if (isYaml(file))
            networkConfig = NetworkConfig.fromYamlFile(file);
        else if (isJson(file))
            networkConfig = NetworkConfig.fromJsonFile(file);
        else
            throw new IllegalArgumentException("cannot read file format. please check the filename has an extension.");

        this.store.put(key, networkConfig);
        this.addNetworkConfigPath(key, file.getPath());
    }
    private void addNetworkConfigPath(String key, String path) {
        this.pathStore.put(key, Path.of(path));
    }
    public NetworkConfig getNetworkConfig(String key) {
        return this.store.get(key);
    }

    public Path getNetworkConfigPath(String key) {
        return this.pathStore.get(key);
    }

    private boolean isJson(File config) {
        return FilenameUtils.getExtension(config.getName()).equals("json");
    }

    private boolean isYaml(File config) {
        return FilenameUtils.getExtension(config.getName()).equals("yaml");
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

                File configFile = ResourceUtils.getFile(entry.getValue());
                configStore.addNetworkConfig(entry.getKey(), configFile);
            }

            return configStore;
        }
    }
}
