package com.planora.backend.common.exception;

public class InvalidAccountReferenceException extends RuntimeException {
    public InvalidAccountReferenceException(String message) {
        super(message);
    }
}
