package com.planora.backend.common.security.jwt;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Set;

public class JwtPurposeValidator implements OAuth2TokenValidator<Jwt> {

    private static final String TOKEN_USE_CLAIM = "toke_use";

    private static final Set<String> ALLOWED_PURPOSES =
        Set.of(JwtTokenPurpose.ACCESS.name(), JwtTokenPurpose.ONBOARDING.name());

    private static final OAuth2Error INVALID_TOKEN_PURPOSE =
        new OAuth2Error(
            "invalid_token_purpose",
            "JWT token purpose is missing or invalid",
            null
        );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String purpose = jwt.getClaimAsString(TOKEN_USE_CLAIM);

        if (purpose == null || !ALLOWED_PURPOSES.contains(purpose)) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN_PURPOSE);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
