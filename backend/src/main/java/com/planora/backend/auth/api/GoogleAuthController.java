package com.planora.backend.auth.api;

import com.planora.backend.auth.api.dto.google.GoogleRegistrationRequest;
import com.planora.backend.auth.api.dto.google.GoogleSessionResponse;
import com.planora.backend.auth.api.dto.login.LoginResponse;
import com.planora.backend.auth.application.oauth.GoogleAccountService;
import com.planora.backend.auth.application.oauth.GoogleRegistrationCommand;
import com.planora.backend.auth.security.oauth2.GoogleAuthProperties;
import com.planora.backend.auth.security.oauth2.PendingGoogleIdentity;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.common.security.jwt.access.AccessTokenIssuer;
import com.planora.backend.common.security.jwt.access.IssuedAccessToken;
import com.planora.backend.common.security.jwt.onboarding.IssuedOnBoardingToken;
import com.planora.backend.common.security.jwt.onboarding.OnBoardingTokenIssuer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("api/auth/google")
public class GoogleAuthController {

    private final GoogleAccountService accountService;
    private final AccessTokenIssuer accessTokenIssuer;
    private final OnBoardingTokenIssuer onBoardingTokenIssuer;
    private final GoogleAuthProperties properties;
    private final CurrentUser currentUser;
    private final Clock clock;

    public GoogleAuthController(GoogleAccountService accountService, AccessTokenIssuer accessTokenIssuer, OnBoardingTokenIssuer onBoardingTokenIssuer, GoogleAuthProperties properties, CurrentUser currentUser, Clock clock) {
        this.accountService = accountService;
        this.accessTokenIssuer = accessTokenIssuer;
        this.onBoardingTokenIssuer = onBoardingTokenIssuer;
        this.properties = properties;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @PostMapping("/session")
    public GoogleSessionResponse session(HttpServletRequest request) {
        verifyOrigin(request);

        PendingGoogleIdentity pending = requirePending(request);

        Optional<UUID> linkedUser = accountService.findLinkedUser(pending.subject());

        if (linkedUser.isPresent()) {
            LoginResponse login = issueToken(linkedUser.get());

            clearPending(request);

            return new GoogleSessionResponse(
                "AUTHENTICATED",
                null,
                null,
                null,
                login
            );
        }

        boolean existingEmail = accountService.emailBelongsToExistingUser(pending.email());

        if (existingEmail) {
            return new GoogleSessionResponse("LINK_REQUIRED",
                pending.email(),
                pending.firstName(),
                pending.lastName(),
                null
            );
        }

        UUID userId = accountService.register(pending);

        LoginResponse login = issueToken(userId);

        clearPending(request);

        return new GoogleSessionResponse(
            "REGISTRATION_REQUIRED",
            pending.email(),
            pending.firstName(),
            pending.lastName(),
            login
        );
    }

    @PostMapping("/link")
    public LoginResponse link(HttpServletRequest request) {
        verifyOrigin(request);

        PendingGoogleIdentity pending = requirePending(request);

        UUID localUserId = currentUser.userId();

        UUID linkedUserId = accountService.link(
            localUserId,
            pending
        );

        LoginResponse response = issueToken(linkedUserId);

        clearPending(request);

        return response;
    }

    private LoginResponse issueToken(UUID userId) {
        if (accountService.isProfileComplete(userId)) {
            IssuedAccessToken token = accessTokenIssuer.issue(userId);

            return new LoginResponse(
                token.token(),
                token.tokenType(),
                token.expiresIn()
            );
        }

        IssuedOnBoardingToken token = onBoardingTokenIssuer.issue(userId);

        return new LoginResponse(
            token.token(),
            token.tokenType(),
            token.expiresIn()
        );
    }

    private PendingGoogleIdentity requirePending(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google authentication session not found"
            );
        }

        Object value = session.getAttribute(
            PendingGoogleIdentity.SESSION_KEY
        );

        if (!(value instanceof PendingGoogleIdentity pending)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google authentication required"
            );
        }

        if (pending.isExpired(clock)) {
            clearPending(request);

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Google authentication expired"
            );
        }

        return pending;
    }

    private void verifyOrigin(HttpServletRequest request) {

        String origin = request.getHeader("Origin");

        if (!properties.frontendOrigin().equals(origin)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Google authentication origin mismatch"
            );
        }
    }

    private void clearPending(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }
}
