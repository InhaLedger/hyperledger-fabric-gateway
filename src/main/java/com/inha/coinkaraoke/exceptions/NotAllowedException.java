package com.inha.coinkaraoke.exceptions;

import org.springframework.http.HttpStatus;

public class NotAllowedException extends GlobalException {

    private static final HttpStatus ERROR_CODE = HttpStatus.UNAUTHORIZED;


    public NotAllowedException() {
        super(ERROR_CODE);
    }

    public NotAllowedException(String reason) {
        super(ERROR_CODE, reason);
    }

    public NotAllowedException(String reason, Throwable cause) {
        super(ERROR_CODE, reason, cause);
    }
}
