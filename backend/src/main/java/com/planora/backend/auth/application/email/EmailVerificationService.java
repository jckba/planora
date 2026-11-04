package com.planora.backend.auth.application.email;

import com.planora.backend.auth.application.security.VerificationCodeHasher;
import com.planora.backend.auth.domain.EmailVerificationChallenge;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.EmailVerificationChallengeRepository;
import com.planora.backend.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailVerificationChallengeRepository challengeRepository;
    private final VerificationCodeHasher codeHasher;
    private final Clock clock;

    public EmailVerificationService(UserRepository userRepository, EmailVerificationChallengeRepository challengeRepository, VerificationCodeHasher codeHasher, Clock clock) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.codeHasher = codeHasher;
        this.clock = clock;
    }

    @Transactional
    public EmailVerificationOutcome execute(String email, String code) {

        if (email == null || email.isBlank()) {
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        if (code == null || !code.matches("\\d{6}")) {
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        Optional<User> candidate = userRepository.findByEmail(email.trim());

        if (candidate.isEmpty()) {
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        User user = userRepository.findByIdForUpdate(candidate.get().getId())
            .orElseThrow(() -> new IllegalStateException("User disappeared during verification"));

        if (user.isEmailVerified()) {
            return EmailVerificationOutcome.of(
                EmailVerificationResult.ALREADY_VERIFIED
            );
        }

        Instant now = clock.instant();

        Optional<EmailVerificationChallenge> candidateChallenge =
            challengeRepository.findLatestUnresolvedByUserIdForUpdate(user.getId());

        if (candidateChallenge.isEmpty()) {
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        EmailVerificationChallenge challenge = candidateChallenge.get();

        if (!challenge.canBeVerifiedAt(now)) {
            if (!challenge.isConsumed() && !challenge.isInvalidated()) {
                challenge.invalidate(now);
                challengeRepository.update(challenge);
            }
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        if (!codeHasher.matches(user.getId(), code, challenge.getCodeHash())) {
            challenge.registerFailedAttempt();

            if (!challenge.hasAttemptsRemaining()) {
                challenge.invalidate(now);
                challengeRepository.update(challenge);

                return EmailVerificationOutcome.of(
                    EmailVerificationResult.TOO_MANY_ATTEMPTS
                );
            }

            challengeRepository.update(challenge);
            return EmailVerificationOutcome.of(
                EmailVerificationResult.INVALID_OR_EXPIRED
            );
        }

        challenge.consume(now);
        challengeRepository.update(challenge);

        user.verifyEmail(now);
        userRepository.update(user);

        return EmailVerificationOutcome.verified(user.getId());
    }
}

