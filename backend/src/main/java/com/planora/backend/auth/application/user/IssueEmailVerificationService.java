package com.planora.backend.auth.application.user;

import com.planora.backend.auth.application.security.VerificationCodeGenerator;
import com.planora.backend.auth.application.security.VerificationCodeHasher;
import com.planora.backend.auth.domain.EmailVerificationChallenge;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.EmailVerificationChallengeRepository;
import com.planora.backend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class IssueEmailVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationChallengeRepository challengeRepository;
    private final VerificationCodeGenerator codeGenerator;
    private final VerificationCodeHasher codeHasher;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final Duration codeValidity;
    private final Duration resendDelay;
    private final int maxAttempts;

    public IssueEmailVerificationService(
        UserRepository userRepository,
        EmailVerificationChallengeRepository challengeRepository,
        VerificationCodeGenerator codeGenerator,
        VerificationCodeHasher codeHasher,
        ApplicationEventPublisher eventPublisher,
        Clock clock,
        @Value("${planora.security.email-verification.code-validity}") Duration codeValidity,
        @Value("${planora.security.email-verification.resend-delay}") Duration resendDelay,
        @Value("${planora.security.email-verification.max-attempts}") int maxAttempts
    ) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.codeGenerator = codeGenerator;
        this.codeHasher = codeHasher;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.codeValidity = codeValidity;
        this.resendDelay = resendDelay;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void execute(UUID userId) {
        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("User email is already verified");
        }

        Instant now = clock.instant();

        if (challengeRepository.existsCreatedSince(userId, now.minus(resendDelay))) {
            throw new IllegalStateException("A verification challenge has been sent recently");
        }

        challengeRepository.invalidateActiveByUserId(userId, now);

        String code = codeGenerator.generate();
        String codeHash = codeHasher.hash(userId, code);

        EmailVerificationChallenge challenge =
            EmailVerificationChallenge.create(
                userId,
                codeHash,
                now,
                codeValidity,
                maxAttempts
            );

        challengeRepository.save(challenge);

        eventPublisher.publishEvent(
            new EmailVerificationRequested(
                user.getId(),
                user.getEmail(),
                code,
                challenge.getExpiresAt()
            )
        );
    }

}
