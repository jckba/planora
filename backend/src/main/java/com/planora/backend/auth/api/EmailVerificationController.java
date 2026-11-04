package com.planora.backend.auth.api;

import com.planora.backend.auth.api.dto.register.EmailVerificationConfirmationResponse;
import com.planora.backend.auth.api.dto.register.EmailVerificationResponse;
import com.planora.backend.auth.api.dto.register.ResendEmailVerificationRequest;
import com.planora.backend.auth.api.dto.register.VerifyEmailRequest;
import com.planora.backend.auth.application.email.EmailVerificationOutcome;
import com.planora.backend.auth.application.email.EmailVerificationService;
import com.planora.backend.auth.application.email.ResendEmailVerificationUseCase;
import com.planora.backend.auth.application.email.EmailVerificationResult;
import com.planora.backend.common.security.jwt.onboarding.IssuedOnBoardingToken;
import com.planora.backend.common.security.jwt.onboarding.OnBoardingTokenIssuer;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email-verification")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;
    private final OnBoardingTokenIssuer onboardingTokenIssuer;
    private final ResendEmailVerificationUseCase resendUseCase;

    public EmailVerificationController(EmailVerificationService emailVerificationService, OnBoardingTokenIssuer onboardingTokenIssuer, ResendEmailVerificationUseCase resendUseCase) {
        this.emailVerificationService = emailVerificationService;
        this.onboardingTokenIssuer = onboardingTokenIssuer;
        this.resendUseCase = resendUseCase;
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@Valid @RequestBody VerifyEmailRequest request) {
        EmailVerificationOutcome outcome = emailVerificationService.execute(
            request.email(),
            request.code()
        );

        if (outcome.result() == EmailVerificationResult.VERIFIED) {
            IssuedOnBoardingToken issuedToken =
                onboardingTokenIssuer.issue(outcome.userId());

            return ResponseEntity.ok(
                new EmailVerificationConfirmationResponse(
                    "El correo ha sido verificado.",
                    issuedToken.token(),
                    issuedToken.tokenType(),
                    issuedToken.expiresIn()
                )
            );
        }

        return ResponseEntity.badRequest().body(
            new EmailVerificationResponse("El codigo no es valido o ha expirado.")
        );
    }

    @PostMapping("/resend")
    public ResponseEntity<EmailVerificationResponse> resend(
        @Valid @RequestBody ResendEmailVerificationRequest request
    ) {
        resendUseCase.execute(request.email());

        return ResponseEntity.accepted().body(
            new EmailVerificationResponse("Si la cuenta requiere verificacion, recibiras instrucciones para verificarla.")
        );
    }

}
