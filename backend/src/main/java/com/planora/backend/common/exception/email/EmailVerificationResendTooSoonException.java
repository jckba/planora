package com.planora.backend.common.exception.email;

public class EmailVerificationResendTooSoonException extends RuntimeException {
    public EmailVerificationResendTooSoonException() {
        super("Email verification resend requested too soon");
    }
}
