package com.planora.backend.common.exception;

public class InvalidExpenseReferenceException extends RuntimeException {
    public InvalidExpenseReferenceException(String message) {
        super(message);
    }
}
