package com.inha.coinkaraoke.users;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public interface HFCAService {

    void enrollAdmin(HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);

    void registerAndEnrollUser(String userId, String userPw, HFCAClient orgCAClient, Wallet orgWallet, String orgMspId);
}
