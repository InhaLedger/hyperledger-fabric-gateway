package com.inha.coinkaraoke.services.proposals;

public enum VoteType {

    up("up"),
    down("down");

    private final String type;

    VoteType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
