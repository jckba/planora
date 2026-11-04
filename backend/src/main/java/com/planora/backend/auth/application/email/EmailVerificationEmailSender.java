package com.planora.backend.auth.application.email;

import java.time.Instant;

public interface EmailVerificationEmailSender {

    void sendVerificationCode(String email, String code, Instant expiresAt);
}
