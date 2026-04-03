package com.mahmoudyoussef.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class DuplicateAliasException extends BaseException {

    public DuplicateAliasException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}