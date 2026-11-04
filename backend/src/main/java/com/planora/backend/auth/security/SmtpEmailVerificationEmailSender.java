package com.planora.backend.auth.security;

import com.planora.backend.auth.application.email.EmailVerificationEmailSender;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class SmtpEmailVerificationEmailSender implements EmailVerificationEmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailVerificationEmailSender.class);
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailVerificationEmailSender(JavaMailSender mailSender, @Value("${planora.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationCode(String email, String code, Instant expiresAt) {
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        Duration remaining = Duration.between(Instant.now(), expiresAt);

        if (remaining.isZero() || remaining.isNegative()) {
            log.warn("Email verification code for {} has already expired. Code not sent.", email);
            return;
        }

        long remainingMinutes = remaining.toMinutes();
        String expirationMessage = remainingMinutes > 0 ? "Este código vence en %d minutos.".formatted(remainingMinutes)
            : "Este código vence en menos de un minuto.";

        SimpleMailMessage message = createVerificationMessage(email, code, expirationMessage);

        mailSender.send(message);
    }

    private @NonNull SimpleMailMessage createVerificationMessage(String email, String code, String expirationMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Verifica tu correo electrónico - Planora");
        message.setText("""
            Hola,

            Tu código de verificación de Planora es: %s

            %s

            Si no solicitaste este código, puedes ignorar este correo.

            Por seguridad, no compartas este código con nadie.
            """.formatted(
            code,
            expirationMessage
        ));
        return message;
    }
}
