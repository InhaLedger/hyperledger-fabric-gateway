package com.inha.coinkaraoke.services.proposals;

import lombok.Data;

@Data
public class ProposalRequest {

    private String userId;
    private String type;
    private Long timestamp;
}
