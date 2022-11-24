package com.inha.coinkaraoke.services.coins.dto;

import lombok.Data;

@Data
public class TransferRequest {

    private String senderId;
    private String senderOrg;
    private String receiverId;
    private Double amounts;

}
