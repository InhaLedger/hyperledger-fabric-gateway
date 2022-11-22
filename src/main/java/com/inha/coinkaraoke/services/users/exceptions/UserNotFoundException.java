package com.inha.coinkaraoke.services.users.exceptions;

import com.inha.coinkaraoke.exceptions.GlobalException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends GlobalException {

    private static final HttpStatus ERROR_CODE = HttpStatus.BAD_REQUEST;

    public UserNotFoundException() {
        super(ERROR_CODE);
    }

    public UserNotFoundException(String reason) {
        super(ERROR_CODE, reason);
    }

    public UserNotFoundException(String reason, Throwable cause) {
        super(ERROR_CODE, reason, cause);
    }
}
