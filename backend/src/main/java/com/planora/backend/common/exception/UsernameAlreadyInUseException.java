package com.planora.backend.common.exception;

public class UsernameAlreadyInUseException extends RuntimeException {
    public UsernameAlreadyInUseException() {
        super("Username is already in use");
    }
}
