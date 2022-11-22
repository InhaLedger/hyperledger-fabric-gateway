package com.inha.coinkaraoke.gateway;

import com.inha.coinkaraoke.config.NetworkConfigStore;
import com.inha.coinkaraoke.exceptions.ChainCodeException;
import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.exceptions.WalletProcessException;
import lombok.RequiredArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class GatewayUtils {

    @Value("${fabric.network.channel}")
    private String CHANNEL_NAME; // static 으로 하면 설정파일에서 못 읽고, 하드코딩 해야함.
    private final WalletManager walletManager;
    private final NetworkConfigStore networkConfigStore;


    public Gateway.Builder getBuilder(String orgId, String userId) {
        Wallet orgWallet = walletManager.getWalletOf(orgId);

        try {
            return Gateway.createBuilder()
                    .identity(orgWallet, userId)
                    .discovery(true)
                    .networkConfig(networkConfigStore.getNetworkConfigPath(orgId));
        } catch (IOException e) {
            throw new WalletProcessException(e.getMessage(), e.getCause());
        }
    }

    public Contract getContract(Gateway gateway, String chaincode, String contract) {
        return gateway
                .getNetwork(CHANNEL_NAME)
                .getContract(chaincode, contract);
    }

    public byte[] query(Contract contract, String fxName, String... args) {
        try {
            return contract.evaluateTransaction(fxName, args);
        } catch (ContractException e) {
            throw new ChainCodeException(e.getMessage(), e.getCause());
        }
    }

    public byte[] submit(Contract contract, String fxName, String... args) {
        try {
            return contract.submitTransaction(fxName, args);
        } catch (ContractException | TimeoutException | InterruptedException e) {
            throw new ChainCodeException(e.getMessage(), e.getCause());
        }
    }
}
