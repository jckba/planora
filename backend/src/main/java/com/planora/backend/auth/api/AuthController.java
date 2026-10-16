package com.planora.backend.auth.api;

import com.planora.backend.auth.api.dto.LoginRequest;
import com.planora.backend.auth.api.dto.LoginResponse;
import com.planora.backend.auth.api.dto.RegisterUserRequest;
import com.planora.backend.auth.application.user.CreateUserCommand;
import com.planora.backend.auth.application.user.CreateUserUseCase;

import com.planora.backend.auth.security.PlanoraUserPrincipal;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.common.security.jwt.JwtProperties;
import com.planora.backend.common.security.jwt.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CreateUserUseCase createUserUseCase;
    private final CurrentUser currentUser;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthenticationManager authenticationManager, CreateUserUseCase createUserUseCase, CurrentUser currentUser, JwtTokenService jwtTokenService, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.createUserUseCase = createUserUseCase;
        this.currentUser = currentUser;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @GetMapping("/me")
    public Map<String, String> me() {
        return Map.of(
            "userId", currentUser.userId().toString()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody RegisterUserRequest request
    ) {
        CreateUserCommand command = new CreateUserCommand(
            request.username(),
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName(),
            request.primaryCurrencyId()
        );

        createUserUseCase.execute(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
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

        String accessToken = jwtTokenService.issueAccessToken(principal.getUserId());

        LoginResponse response = new LoginResponse(
            accessToken,
            "Bearer",
            jwtProperties.accessTokenTtl().toSeconds()
        );

        return ResponseEntity.ok(response);
    }
}
