package com.planora.backend.auth.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;

@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleAuthProperties properties;
    private final Clock clock;

    public GoogleLoginSuccessHandler(GoogleAuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (oidcUser.getSubject() == null ||
            oidcUser.getSubject().isBlank() ||
            oidcUser.getEmail() == null ||
            oidcUser.getEmail().isBlank() ||
            !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {

            request.getSession().invalidate();
            response.sendRedirect(
                properties.frontendOrigin()
                    + "/auth/google/callback?error=email_not_verified"
            );
            return;
        }

        PendingGoogleIdentity pending =
            new PendingGoogleIdentity(
                oidcUser.getSubject(),
                oidcUser.getEmail(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName(),
                clock.instant()
            );

        request.getSession(true).setAttribute(
            PendingGoogleIdentity.SESSION_KEY,
            pending
        );

        response.sendRedirect(
            properties.frontendOrigin()
                + "/auth/google/callback"
        );
    }
}

