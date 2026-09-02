package com.planora.backend.auth.application.user;

import com.planora.backend.auth.domain.User;

public interface CreateUserUseCase {
    User execute(CreateUserCommand command);
}
