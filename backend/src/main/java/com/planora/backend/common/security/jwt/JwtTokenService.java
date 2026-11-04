package com.planora.backend.common.security.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String TOKEN_USE_CLAIM = "token_use";

    private final RSAPrivateKey signingKey;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenService(RSAPrivateKey signingKey, JwtProperties jwtProperties, Clock clock) {
        this.signingKey = signingKey;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String issueAccessToken(UUID userId) {
        return issueToken(userId, JwtTokenPurpose.ACCESS, jwtProperties.accessTokenTtl());
    }

    public String issueOnboardingToken(UUID userId) {
        return issueToken(userId, JwtTokenPurpose.ONBOARDING, jwtProperties.onboardingTokenTtl());
    }

    private String issueToken(
        UUID userId,
        JwtTokenPurpose purpose,
        Duration ttl
    ) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(purpose, "purpose cannot be null");

        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(ttl);

        return Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(userId.toString())
            .claim(TOKEN_USE_CLAIM, purpose.name())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey, Jwts.SIG.RS256)
            .compact();
    }

}
