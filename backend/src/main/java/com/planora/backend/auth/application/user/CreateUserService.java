package com.planora.backend.auth.application.user;

import com.planora.backend.auth.application.security.PasswordHasher;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.common.exception.EmailAlreadyInUseException;
import com.planora.backend.common.exception.UsernameAlreadyInUseException;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase{

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }


    @Override
    public User execute(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyInUseException();
        }

        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyInUseException();
        }

        String passwordHash = passwordHasher.hash(command.password());

        User user = User.create(
            command.username(),
            command.email(),
            passwordHash,
            command.firstName(),
            command.lastName(),
            command.primaryCurrencyId()
        );

        return userRepository.save(user);
    }
}
