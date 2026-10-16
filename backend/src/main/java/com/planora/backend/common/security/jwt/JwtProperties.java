package com.planora.backend.common.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "planora.security.jwt")
public record JwtProperties(
    String issuer,
    Duration accessTokenTtl,
    String privateKeyLocation,
    String publicKeyLocation
) {
    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("JWT issuer is required");
        }
        if (accessTokenTtl == null
            || accessTokenTtl.isZero()
            || accessTokenTtl.isNegative()) {
            throw new IllegalArgumentException("JWT access token TTL is required and must be positive");
        }
        if (privateKeyLocation == null || privateKeyLocation.isBlank()
            || publicKeyLocation == null || publicKeyLocation.isBlank()) {
            throw new IllegalArgumentException("JWT private and public key locations are required");
        }
    }
}
