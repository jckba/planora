package com.planora.backend.auth.application.user.register;

import com.planora.backend.auth.application.email.IssueEmailVerificationService;
import com.planora.backend.auth.application.user.create.CreateUserCommand;
import com.planora.backend.auth.application.user.create.CreateUserUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegisterLocalUserUseCase {

    private final CreateUserUseCase createUserUseCase;
    private final IssueEmailVerificationService issueEmailVerificationService;

    public RegisterLocalUserUseCase(CreateUserUseCase createUserUseCase, IssueEmailVerificationService issueEmailVerificationService) {
        this.createUserUseCase = createUserUseCase;
        this.issueEmailVerificationService = issueEmailVerificationService;
    }

    @Transactional
    public void execute(CreateUserCommand command) {
        UUID userId = createUserUseCase.execute(command).getId();
        issueEmailVerificationService.execute(userId);
    }
}
