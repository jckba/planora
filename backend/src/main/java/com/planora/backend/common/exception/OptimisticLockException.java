package com.planora.backend.common.exception;

public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }
}
