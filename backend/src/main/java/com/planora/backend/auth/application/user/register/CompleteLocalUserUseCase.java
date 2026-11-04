package com.planora.backend.auth.application.user.register;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.common.security.CurrentUser;
import com.planora.backend.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class CompleteLocalUserUseCase {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public CompleteLocalUserUseCase(UserRepository userRepository, CurrencyRepository currencyRepository, CurrentUser currentUser, Clock clock) {
        this.userRepository = userRepository;
        this.currencyRepository = currencyRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional
    public void execute(CompleteLocalUserCommand command) {
        UUID userId = currentUser.userId();

        User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("User email not verified");
        }

        String username = command.username().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is unavailable");
        }

        if (!currencyRepository.existsById(command.primaryCurrencyId())) {
            throw new IllegalArgumentException("Currency not found");
        }

        user.completeProfile(
            username,
            command.primaryCurrencyId(),
            clock.instant()
        );

        userRepository.update(user);
    }
}
