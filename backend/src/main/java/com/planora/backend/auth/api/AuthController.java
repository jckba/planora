package com.planora.backend.auth.api;

import com.planora.backend.auth.api.dto.login.LoginRequest;
import com.planora.backend.auth.api.dto.login.LoginResponse;
import com.planora.backend.auth.api.dto.register.CompleteProfileRequest;
import com.planora.backend.auth.api.dto.register.UserRegistrationRequest;
import com.planora.backend.auth.application.user.create.CreateUserCommand;

import com.planora.backend.auth.application.user.register.CompleteProfileCommand;
import com.planora.backend.auth.application.user.register.CompleteProfileUseCase;
import com.planora.backend.auth.application.user.register.RegisterLocalUserUseCase;
import com.planora.backend.auth.security.PlanoraUserPrincipal;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.common.security.jwt.access.AccessTokenIssuer;
import com.planora.backend.common.security.jwt.access.IssuedAccessToken;
import com.planora.backend.common.security.jwt.onboarding.IssuedOnBoardingToken;
import com.planora.backend.common.security.jwt.onboarding.OnBoardingTokenIssuer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final RegisterLocalUserUseCase registerLocalUserUseCase;
    private final CompleteProfileUseCase completeProfileUseCase;
    private final CurrentUser currentUser;
    private final AccessTokenIssuer accessTokenIssuer;
    private final OnBoardingTokenIssuer onBoardingTokenIssuer;

    public AuthController(AuthenticationManager authenticationManager, RegisterLocalUserUseCase registerLocalUserUseCase, CompleteProfileUseCase completeProfileUseCase, CurrentUser currentUser, AccessTokenIssuer accessTokenIssuer, OnBoardingTokenIssuer onBoardingTokenIssuer) {
        this.authenticationManager = authenticationManager;
        this.registerLocalUserUseCase = registerLocalUserUseCase;
        this.completeProfileUseCase = completeProfileUseCase;
        this.currentUser = currentUser;
        this.accessTokenIssuer = accessTokenIssuer;
        this.onBoardingTokenIssuer = onBoardingTokenIssuer;
    }

    @GetMapping("/me")
    public Map<String, String> me() {
        return Map.of(
            "userId", currentUser.userId().toString()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody UserRegistrationRequest request
    ) {
        CreateUserCommand command = new CreateUserCommand(
            request.firstName(),
            request.lastName(),
            request.email(),
            request.password()
        );

        registerLocalUserUseCase.execute(command);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<LoginResponse> completeRegistration(
        @Valid @RequestBody CompleteProfileRequest request
    ) {
        completeProfileUseCase.execute(
            new CompleteProfileCommand(
                request.username(),
                request.primaryCurrencyId()
            )
        );

        IssuedAccessToken issuedToken = accessTokenIssuer.issue(
            currentUser.userId()
        );

        return ResponseEntity.ok(
            new LoginResponse(
                issuedToken.token(),
                issuedToken.tokenType(),
                issuedToken.expiresIn()
            )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                request.identifier().trim(),
                request.password()
            )
        );

        if (!(authentication.getPrincipal() instanceof PlanoraUserPrincipal principal)) {
            throw new IllegalStateException(
                "Unexpected authenticated principal"
            );
        }

        if (principal.isProfileComplete()) {
            IssuedAccessToken issuedToken = accessTokenIssuer.issue(principal.getUserId());
            return ResponseEntity.ok(
                new LoginResponse(
                    issuedToken.token(),
                    issuedToken.tokenType(),
                    issuedToken.expiresIn()
                )
            );
        }

        IssuedOnBoardingToken issuedToken =
            onBoardingTokenIssuer.issue(principal.getUserId());

        return ResponseEntity.ok(
            new LoginResponse(
                issuedToken.token(),
                issuedToken.tokenType(),
                issuedToken.expiresIn()
            )
        );
    }
}
