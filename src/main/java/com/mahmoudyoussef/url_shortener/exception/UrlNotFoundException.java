package com.mahmoudyoussef.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class UrlNotFoundException extends BaseException {

    public UrlNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}