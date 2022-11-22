package com.inha.coinkaraoke.services.users.impl;

import com.inha.coinkaraoke.services.users.WalletManager;
import com.inha.coinkaraoke.services.users.exceptions.WalletProcessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.impl.identity.FileSystemWalletStore;
import org.hyperledger.fabric.gateway.impl.identity.WalletImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletManagerImpl implements WalletManager {

    private static final String PATH = "wallets";

    private final Map<String, Wallet> wallets = new HashMap<>();

    public Wallet getWalletOf(String orgMspId) {

        if(!this.wallets.containsKey(orgMspId)) {

            WalletImpl wallet;
            try {
                wallet = new WalletImpl(new FileSystemWalletStore(Path.of(PATH, orgMspId)));
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new WalletProcessException();
            }
            this.wallets.put(orgMspId, wallet);
        }

        return wallets.get(orgMspId);
    }
}
