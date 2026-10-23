package com.planora.backend.auth.application.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Component
public class VerificationCodeHasher {

    private final SecretKeySpec secretKey;

    public VerificationCodeHasher(
        @Value("${planora.security.email-verification.hmac-key}")
        String encodedKey
    ) {
        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "Email verification HMAC key must be at least 32 bytes long"
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String hash(UUID userId, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            String payload = userId + ":" + code;

            byte[] digest = mac.doFinal(
                payload.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(digest);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                "Unable to hash email verification code"
            );
        }
    }

    public boolean matches(UUID userId, String code, String expectedHash) {
        byte[] actual = Base64.getDecoder().decode(hash(userId, code));
        byte[] expected = Base64.getDecoder().decode(expectedHash);

        return MessageDigest.isEqual(actual, expected);
    }

}
