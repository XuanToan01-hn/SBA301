package com.buildings.exception.auth;

public class UnAuthorException extends RuntimeException {
    public UnAuthorException(String message) {
        super(message);
    }
}
