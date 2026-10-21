package com.planora.backend.auth.application.user;

import com.planora.backend.auth.application.security.PasswordHasher;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.common.exception.EmailAlreadyInUseException;
import com.planora.backend.common.exception.UsernameAlreadyInUseException;
import org.springframework.stereotype.Service;

@Service
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User execute(CreateUserCommand command) {
        String email = command.email().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException();
        }

        String passwordHash = passwordHasher.hash(command.password());

        User user = User.createPendingRegistration(
            command.email(),
            passwordHash,
            command.firstName(),
            command.lastName()
        );

        return userRepository.save(user);
    }
}
