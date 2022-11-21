package com.inha.coinkaraoke.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends GlobalException {

    private static final HttpStatus ERROR_CODE = HttpStatus.BAD_REQUEST;

    public BadRequestException() {
        super(ERROR_CODE);
    }

    public BadRequestException(String reason) {
        super(ERROR_CODE, reason);
    }

    public BadRequestException(String reason, Throwable cause) {
        super(ERROR_CODE, reason, cause);
    }
}
