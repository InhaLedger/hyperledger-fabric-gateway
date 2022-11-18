package com.inha.coinkaraoke.users;

import org.hyperledger.fabric.gateway.Wallet;

public interface WalletManager {

    Wallet getWalletOf(String orgMspId);
}
