package com.inha.coinkaraoke.wallets.exceptions;

public class WalletProcessException extends RuntimeException {

    public WalletProcessException() {
        super("cannot access wallet!");
    }

    public WalletProcessException(String message) {
        super(message);
    }
}
