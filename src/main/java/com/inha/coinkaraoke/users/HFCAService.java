package com.inha.coinkaraoke.users;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public interface HFCAService {

    void enrollAdmin(HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);

    void registerAndEnrollUser(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);

    void revokeUser(String userId, String reason, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);

    void reEnroll(String userId, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);
}

