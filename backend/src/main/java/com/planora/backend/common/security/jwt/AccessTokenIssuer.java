package com.planora.backend.common.security.jwt;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessTokenIssuer {
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AccessTokenIssuer(JwtTokenService jwtTokenService, JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    public IssuedAccessToken issue(UUID userId) {

        return new IssuedAccessToken(
            jwtTokenService.issueAccessToken(userId),
            ")Bearer",
            jwtProperties.accessTokenTtl().toSeconds()
        );
    }
}
