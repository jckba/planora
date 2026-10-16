package com.planora.backend.common.security.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final RSAPrivateKey signingKey;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public JwtTokenService(RSAPrivateKey signingKey, JwtProperties jwtProperties, Clock clock) {
        this.signingKey = signingKey;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    public String issueAccessToken(UUID userId) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());

        return Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject(userId.toString())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey, Jwts.SIG.RS256)
            .compact();
    }
}
