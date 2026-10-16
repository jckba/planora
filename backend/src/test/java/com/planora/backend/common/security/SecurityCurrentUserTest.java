package com.planora.backend.common.security;

import com.planora.backend.auth.security.PlanoraUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityCurrentUserTest {

    private final SecurityCurrentUser currentUser =
        new SecurityCurrentUser();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnUserIdFromJwt() {

        UUID userId = UUID.randomUUID();

        Jwt jwt = createJwt(userId.toString());

        JwtAuthenticationToken authentication =
            new JwtAuthenticationToken(jwt, List.of());

        setAuthentication(authentication);

        assertEquals(userId, currentUser.userId());
    }

    @Test
    void shouldReturnUserIdFromLocalPrincipal() {

        UUID userId = UUID.randomUUID();

        PlanoraUserPrincipal principal =
            mock(PlanoraUserPrincipal.class);

        when(principal.getUserId()).thenReturn(userId);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of()
            );

        setAuthentication(authentication);

        assertEquals(userId, currentUser.userId());
    }

    @Test
    void shouldRejectMissingAuthentication() {

        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            currentUser::userId
        );
    }

    @Test
    void shouldRejectInvalidJwtSubject() {

        Jwt jwt = createJwt("invalid-uuid");

        setAuthentication(
            new JwtAuthenticationToken(jwt, List.of())
        );

        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            currentUser::userId
        );
    }

    @Test
    void shouldRejectUnsupportedPrincipal() {

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                "unexpected-principal",
                null,
                List.of()
            );

        setAuthentication(authentication);

        assertThrows(
            AuthenticationCredentialsNotFoundException.class,
            currentUser::userId
        );
    }

    private void setAuthentication(
        Authentication authentication
    ) {
        SecurityContext context =
            SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Jwt createJwt(String subject) {

        Instant now = Instant.now();

        return Jwt.withTokenValue("test-token")
            .header("alg", "RS256")
            .subject(subject)
            .issuer("http://localhost:8080")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .build();
    }
}
