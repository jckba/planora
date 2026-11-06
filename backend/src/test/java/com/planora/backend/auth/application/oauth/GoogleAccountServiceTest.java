package com.planora.backend.auth.application.oauth;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.ExternalIdentityRepository;
import com.planora.backend.auth.repository.UserRepository;
import com.planora.backend.auth.security.oauth2.PendingGoogleIdentity;
import com.planora.backend.common.exception.oauth.GoogleIdentityConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class GoogleAccountServiceTest {

    private UserRepository userRepository;
    private ExternalIdentityRepository identityRepository;
    private GoogleAccountService googleAccountService;

    private PendingGoogleIdentity google;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        identityRepository = mock(ExternalIdentityRepository.class);

        googleAccountService = new GoogleAccountService(userRepository, identityRepository);

        google = new PendingGoogleIdentity(
            "google-sub-12345",
            "test@example.com",
            "Test",
            "User",
            Instant.now()
        );
    }

    @Test
    void shouldLinkMatchingVerifiedIdentity() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(user.getEmail())
            .thenReturn("test@example.com");

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        UUID result = googleAccountService.link(userId, google);

        assertEquals(userId, result);

        verify(identityRepository).save(
            argThat(identity ->
                identity.userId().equals(userId)
                    && identity.providerSubject()
                    .equals(google.subject())
            )
        );
    }

    @Test
    void shouldRejectLinkWithDifferentEmail() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(user.getEmail())
            .thenReturn("another@example.com");

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        assertThrows(
            GoogleIdentityConflictException.class,
            () -> googleAccountService.link(userId, google)
        );

        verify(identityRepository, never()).save(any());
    }
}
