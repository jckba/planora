package com.planora.backend.common.security;

import com.planora.backend.auth.security.PlanoraUserPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityCurrentUser implements CurrentUser {

    @Override
    public UUID userId() {
        Authentication authentication =
            SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null ||
            !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException(
                "No authentication user found"
            );
        }

        if (authentication.getPrincipal() instanceof PlanoraUserPrincipal principal) {
            return principal.getUserId();
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String subject = jwtAuth.getToken().getSubject();

            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException e) {
                throw new AuthenticationCredentialsNotFoundException(
                    "Invalid JWT subject", e
                );
            }
        }

        throw new AuthenticationCredentialsNotFoundException(
            "Unsupported authentication type"
        );
    }
}
