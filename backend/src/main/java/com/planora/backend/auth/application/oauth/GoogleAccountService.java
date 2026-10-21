package com.planora.backend.auth.application.oauth;

import com.planora.backend.auth.domain.ExternalIdentity;
import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.ExternalIdentityRepository;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.auth.security.oauth2.PendingGoogleIdentity;
import com.planora.backend.common.exception.oauth.GoogleIdentityConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAccountService {

    private static final String GOOGLE_PROVIDER = "GOOGLE";

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
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    @Transactional
    public UUID register(PendingGoogleIdentity google, GoogleRegistrationCommand command) {

        String email = normalizeEmail(google.email());
        String username = normalizeUsername(command.username());

        if (identityRepository.findUserIdByGoogleSubject(google.subject())
            .isPresent()) {
            throw new GoogleIdentityConflictException(
                "Google identity already registered"
            );
        }

        if (userRepository.existsByEmail(email)) {
            throw new GoogleIdentityConflictException(
                "Existing local account requires linking"
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new GoogleIdentityConflictException(
                "Username is unavailable"
            );
        }

        User user = User.createExternal(
            username,
            email,
            command.firstName().trim(),
            command.lastName().trim(),
            command.primaryCurrencyId()
        );

        userRepository.save(user);

        identityRepository.save(
            ExternalIdentity.google(user.getId(), google.subject())
        );

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

        if (!localUser.isEmailVerified()) {
            throw new GoogleIdentityConflictException(
                "Local account email must be verified."
            );
        }


        if (!normalizeEmail(localUser.getEmail()).equals(normalizeEmail(google.email()))) {
            throw new GoogleIdentityConflictException(
                "Google email does not match local account"
            );
        }

        if (identityRepository.findUserIdByGoogleSubject(google.subject()).isPresent()) {
            throw new GoogleIdentityConflictException(
                "Google identity already linked"
            );
        }

        if (identityRepository.existsByUserIdAndProvider(localUserId, GOOGLE_PROVIDER)) {
            throw new GoogleIdentityConflictException(
                "User already has a Google identity"
            );
        }

        identityRepository.save(
            ExternalIdentity.google(localUserId, google.subject())
        );

        return localUserId;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }
}
