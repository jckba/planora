package com.planora.backend.common.exception.oauth;

public class GoogleIdentityConflictException extends RuntimeException {
    public GoogleIdentityConflictException(String message) {
        super(message);
    }
}
