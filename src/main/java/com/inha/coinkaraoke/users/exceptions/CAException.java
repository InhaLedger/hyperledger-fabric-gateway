package com.inha.coinkaraoke.users.exceptions;

import com.inha.coinkaraoke.exceptions.GlobalException;
import org.springframework.http.HttpStatus;


public class CAException extends GlobalException {

    private static final HttpStatus ERROR_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    public CAException(String message) {
        super(ERROR_CODE, message);
    }

    public CAException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
