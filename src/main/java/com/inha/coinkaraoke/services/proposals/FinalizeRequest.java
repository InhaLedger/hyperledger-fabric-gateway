package com.inha.coinkaraoke.services.proposals;

import lombok.Data;

import java.util.Objects;

@Data
public class FinalizeRequest {

    private String userId;
    private Long timestamp;
    private Double rewardPerProposal;
    private Integer batchSize;

    public boolean from(String userId) {
        return Objects.equals(this.userId, userId);
    }


    public String getTimestamp() {
        return String.valueOf(timestamp);
    }

    public String getRewardPerProposal() {
        return String.valueOf(rewardPerProposal);
    }

    public String getBatchSize() {
        return String.valueOf(batchSize);
    }
}
