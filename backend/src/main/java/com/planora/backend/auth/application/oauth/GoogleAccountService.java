package com.planora.backend.auth.application.oauth;

import com.planora.backend.auth.domain.ExternalIdentity;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.ExternalIdentityRepository;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.auth.security.oauth2.PendingGoogleIdentity;
import com.planora.backend.common.exception.oauth.GoogleIdentityConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAccountService {

    private final UserRepository userRepository;
    private final ExternalIdentityRepository identityRepository;

    public GoogleAccountService(UserRepository userRepository, ExternalIdentityRepository identityRepository) {
        this.userRepository = userRepository;
        this.identityRepository = identityRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UUID> findLinkedUser(String googleSubject) {
        return identityRepository.findUserIdByGoogleSubject(googleSubject);
    }

    @Transactional(readOnly = true)
    public boolean emailBelongsToExistingUser(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UUID register(PendingGoogleIdentity google, GoogleRegistrationCommand command) {

        if (identityRepository.findUserIdByGoogleSubject(google.subject())
            .isPresent()) {
            throw new GoogleIdentityConflictException(
                "Google identity already registered"
            );
        }

        if (userRepository.existsByEmail(google.email())) {
            throw new GoogleIdentityConflictException(
                "Existing local account requires linking"
            );
        }

        if (userRepository.existsByUsername(command.username())) {
            throw new GoogleIdentityConflictException(
                "Username is unavailable"
            );
        }

        User user = User.createExternal(
            command.username(),
            google.email(),
            command.firstName(),
            command.lastName(),
            command.primaryCurrencyId()
        );

        userRepository.save(user);

        ExternalIdentity identity = ExternalIdentity.google(
            user.getId(),
            google.subject()
        );

        identityRepository.save(identity);

        return user.getId();
    }

    @Transactional
    public UUID link(
        UUID localUserId,
        PendingGoogleIdentity google
    ) {
        User localUser = userRepository.findById(localUserId)
            .orElseThrow(() -> new GoogleIdentityConflictException(
                "Local user not found"
            ));

        if (!localUser.getEmail().equalsIgnoreCase(google.email())) {
            throw new GoogleIdentityConflictException(
                "Google email does not match local account"
            );
        }

        Optional<UUID> existingIdentity = identityRepository.findUserIdByGoogleSubject(
            google.subject()
        );

        if (existingIdentity.isPresent()) {
            throw new GoogleIdentityConflictException(
                "Google identity already linked"
            );
        }

        if (identityRepository.existsByUserIdAndProvider(localUserId, "GOOGLE")) {
            throw new GoogleIdentityConflictException(
                "User already has a Google identity"
            );
        }

        identityRepository.save(
            ExternalIdentity.google(
                localUserId,
                google.subject()
            )
        );

        return localUserId;
    }


}
