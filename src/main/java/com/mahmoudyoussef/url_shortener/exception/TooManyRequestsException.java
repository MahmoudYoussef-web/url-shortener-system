package com.mahmoudyoussef.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends BaseException {

    public TooManyRequestsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS.value());
    }
}