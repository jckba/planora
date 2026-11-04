package com.planora.backend.common.security.jwt.onboarding;

import com.planora.backend.common.security.jwt.JwtProperties;
import com.planora.backend.common.security.jwt.JwtTokenService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OnBoardingTokenIssuer {

    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;


    public OnBoardingTokenIssuer(JwtTokenService jwtTokenService, JwtProperties jwtProperties) {
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    public IssuedOnBoardingToken issue(UUID userId) {

        return new IssuedOnBoardingToken(
            jwtTokenService.issueOnboardingToken(userId),
            "Bearer",
            jwtProperties.onboardingTokenTtl().toSeconds()
        );
    }
}
