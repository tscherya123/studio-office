package com.fetty.studiooffice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserImageNotFoundException extends RuntimeException {
    public UserImageNotFoundException(String message) {
        super(message);
    }

    public UserImageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}