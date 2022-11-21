package com.inha.coinkaraoke.exceptions;

import org.springframework.http.HttpStatus;

public class ChainCodeException extends GlobalException {

    private static final HttpStatus ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    public ChainCodeException() {
        super(ERROR_CODE);
    }

    public ChainCodeException(String reason) {
        super(ERROR_CODE, reason);
    }

    public ChainCodeException(String reason, Throwable cause) {
        super(ERROR_CODE, reason, cause);
    }
}
