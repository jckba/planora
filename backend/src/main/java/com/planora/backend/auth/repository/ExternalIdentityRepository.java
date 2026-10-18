package com.planora.backend.auth.repository;

import com.planora.backend.auth.domain.ExternalIdentity;

import java.util.Optional;
import java.util.UUID;

public interface ExternalIdentityRepository {

    Optional<UUID> findUserIdByGoogleSubject(String googleSubject);

    boolean existsByUserIdAndProvider(UUID userId, String provider);

    ExternalIdentity save(ExternalIdentity externalIdentity);
}
