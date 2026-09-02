package com.planora.backend.common.security;

import com.planora.backend.auth.security.PlanoraUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityCurrentUser implements CurrentUser {
    @Override
    public UUID userId() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authentication user found");
        }

        if (!(authentication.getPrincipal() instanceof PlanoraUserPrincipal principal)) {
            throw new IllegalStateException("Unexpected authentication principal");
        }

        return principal.getUserId();
    }
}
