package com.inha.coinkaraoke.users.impl;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.users.HFCAClientManager;
import com.inha.coinkaraoke.users.exceptions.CAException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.CAInfo;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;

@Component
@Slf4j
@RequiredArgsConstructor
public class HFCAClientManagerImpl implements HFCAClientManager {

    private final NetworkConfigStore networkConfigStore;

    public HFCAClient getCAClient(String orgMspId) {

        NetworkConfig networkConfig = this.networkConfigStore.getNetworkConfig(orgMspId);
        CAInfo caInfo = networkConfig.getOrganizationInfo(orgMspId)
                .getCertificateAuthorities().get(0);

        try {
            return HFCAClient.createNewInstance(caInfo);

        } catch (MalformedURLException | InvalidArgumentException e) {

            throw new CAException("cannot establish the CA client.", e.getCause());
        }
    }
}
