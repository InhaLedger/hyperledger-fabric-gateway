package com.inha.coinkaraoke.users;

import org.hyperledger.fabric_ca.sdk.HFCAClient;

public interface HFCAClientManager {

    HFCAClient getCAClient(String orgMspId);
}
