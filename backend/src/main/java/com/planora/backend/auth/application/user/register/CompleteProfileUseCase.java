package com.planora.backend.auth.application.user.register;

import com.planora.backend.auth.domain.ExternalIdentity;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.ExternalIdentityRepository;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CompleteProfileUseCase {

    private static final String GOOGLE_PROVIDER = "GOOGLE";

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final ExternalIdentityRepository identityRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public CompleteProfileUseCase(UserRepository userRepository, CurrencyRepository currencyRepository, ExternalIdentityRepository identityRepository, CurrentUser currentUser, Clock clock) {
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.identityRepository = identityRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional
    public void execute(CompleteProfileCommand command) {
        UUID userId = currentUser.userId();

        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("User email not verified");
        }

        if (user.isProfileComplete()) {
            throw new IllegalStateException("User profile is already complete");
        }

        String username = command.username().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is unavailable");
        }

        if (!currencyRepository.existsById(command.primaryCurrencyId())) {
            throw new IllegalArgumentException("Currency not found");
        }

        boolean googleAccount = identityRepository.existsByUserIdAndProvider(userId, GOOGLE_PROVIDER);

        Instant now = clock.instant();

        if (googleAccount) {
            validateGooglePersonalData(command);

            user.updateFirstAndLastName(
                command.firstName(),
                command.lastName(),
                now
            );
        }

        user.completeProfile(
            username,
            command.primaryCurrencyId(),
            clock.instant()
        );

        userRepository.update(user);
    }

    private void validateGooglePersonalData(CompleteProfileCommand command) {
        if (command.firstName() == null || command.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (command.lastName() == null || command.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }
}
