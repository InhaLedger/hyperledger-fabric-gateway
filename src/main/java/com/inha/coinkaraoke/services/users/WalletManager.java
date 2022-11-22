package com.inha.coinkaraoke.services.users;

import org.hyperledger.fabric.gateway.Wallet;

public interface WalletManager {

    Wallet getWalletOf(String orgMspId);
}
