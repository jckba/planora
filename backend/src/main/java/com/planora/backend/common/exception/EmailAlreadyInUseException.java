package com.planora.backend.common.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Email is already in use.");
    }
}
