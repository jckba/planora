package com.planora.backend.auth.application.email;

import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.common.exception.email.EmailAlreadyVerifiedException;
import com.planora.backend.common.exception.email.EmailVerificationResendTooSoonException;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailVerificationUseCase {

    private final UserRepository userRepository;
    private final IssueEmailVerificationService issueService;

    public ResendEmailVerificationUseCase(UserRepository userRepository, IssueEmailVerificationService issueService) {
        this.userRepository = userRepository;
        this.issueService = issueService;
    }

    public void execute(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email.trim())
            .filter(user -> !user.isEmailVerified())
            .ifPresent(user -> {
                    try {
                        issueService.execute(user.getId());
                    } catch (EmailVerificationResendTooSoonException
                             | EmailAlreadyVerifiedException ignored) {

                    }
                }
            );

    }
}
