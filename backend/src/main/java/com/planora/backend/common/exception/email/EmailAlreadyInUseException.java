package com.planora.backend.common.exception.email;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException() {
        super("Email is already in use.");
    }
}
