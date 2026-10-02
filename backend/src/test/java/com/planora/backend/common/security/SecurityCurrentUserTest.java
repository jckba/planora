package com.planora.backend.common.security;

import com.planora.backend.auth.security.PlanoraUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityCurrentUserTest {
    private final SecurityCurrentUser currentUser = new SecurityCurrentUser();

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnAuthenticatedPrincipalId() {
        UUID userId = UUID.randomUUID();
        PlanoraUserPrincipal principal =
            new PlanoraUserPrincipal(userId, "user@planora.test", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of())
        );

        assertEquals(userId, currentUser.userId());
    }

    @Test
    void shouldRejectMissingAuthentication() {
        assertThrows(IllegalStateException.class, currentUser::userId);
    }

    @Test
    void shouldRejectUnauthenticatedPrincipal() {
        PlanoraUserPrincipal principal =
            new PlanoraUserPrincipal(UUID.randomUUID(), "user@planora.test", "hash", List.of());
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.unauthenticated(principal, null)
        );

        assertThrows(IllegalStateException.class, currentUser::userId);
    }

    @Test
    void shouldRejectUnexpectedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated("user", null, List.of())
        );

        assertThrows(IllegalStateException.class, currentUser::userId);
    }
}
