package com.buildings.exception.user;

public class UsernameExist extends RuntimeException {
    public UsernameExist(String message) {
        super(message);
    }
}
