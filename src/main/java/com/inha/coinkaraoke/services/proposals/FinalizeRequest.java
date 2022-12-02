package com.inha.coinkaraoke.services.proposals;

import java.util.Objects;
import lombok.Data;

@Data
public class FinalizeRequest {

    private String userId;

    public boolean from(String userId) {
        return Objects.equals(this.userId, userId);
    }
}
