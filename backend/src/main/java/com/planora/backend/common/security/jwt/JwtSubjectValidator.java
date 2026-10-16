package com.planora.backend.common.security.jwt;


import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class JwtSubjectValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_SUBJECT =
        new OAuth2Error(
            "invalid_subject",
            "JWT subject must be a valid UUID",
            null
        );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            return invalid();
        }

        try {
            UUID userId = UUID.fromString(subject);

            if (!userId.toString().equalsIgnoreCase(subject)) {
                return invalid();
            }

            return OAuth2TokenValidatorResult.success();

        } catch (IllegalArgumentException e) {
            return invalid();
        }
    }

    private OAuth2TokenValidatorResult invalid() {
        return OAuth2TokenValidatorResult.failure(
            INVALID_SUBJECT
        );
    }
}
