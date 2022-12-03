package com.inha.coinkaraoke.services.proposals;


import lombok.Data;

@Data
public class VoteRequest {

    private Double amounts;
    private String userId;
    private Long timestamp;
    private VoteType type;
}
