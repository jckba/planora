package com.planora.backend.auth.security;

import com.planora.backend.auth.application.email.EmailVerificationEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SmtpEmailVerificationEmailSender implements EmailVerificationEmailSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public SmtpEmailVerificationEmailSender(JavaMailSender mailSender, @Value("${planora.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationCode(String email, String code, Instant expiresAt) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Verifica tu correo electrónico - Planora");
        message.setText("""
            Hola,

            Tu código de verificación de Planora es: %s

            El código vence a las: %s

            Si no solicitaste este código, puedes ignorar este correo.

            Por seguridad, no compartas este código con nadie.
            """.formatted(
            code,
            expiresAt
        ));
        mailSender.send(message);
    }
}
