package com.planora.backend.auth.api;

import com.planora.backend.auth.api.dto.LoginRequest;
import com.planora.backend.auth.api.dto.RegisterUserRequest;
import com.planora.backend.auth.application.user.CreateUserCommand;
import com.planora.backend.auth.application.user.CreateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CreateUserUseCase createUserUseCase;

    public AuthController(AuthenticationManager authenticationManager, CreateUserUseCase createUserUseCase) {
        this.authenticationManager = authenticationManager;
        this.createUserUseCase = createUserUseCase;
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
    public ResponseEntity<Void> login(
        @Valid @RequestBody LoginRequest request
    ) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );
        return ResponseEntity.ok().build();
    }
}
