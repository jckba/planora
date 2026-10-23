package com.planora.backend.auth.application.user;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EmailVerificationEventListener {

    private final EmailVerificationEmailSender emailSender;

    public EmailVerificationEventListener(
        EmailVerificationEmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailVerificationRequested(EmailVerificationRequested event) {
        emailSender.sendVerificationCode(
            event.email(),
            event.code(),
            event.expiresAt()
        );
    }
}
