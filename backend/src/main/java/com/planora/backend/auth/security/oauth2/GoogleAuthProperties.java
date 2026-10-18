package com.planora.backend.auth.security.oauth2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "planora.security.google")
public record GoogleAuthProperties(
    String frontendOrigin
) {
    public GoogleAuthProperties {
        if (frontendOrigin == null ||
            frontendOrigin.isBlank()) {
            throw new IllegalArgumentException("Google frontend origin must be provided");
        }

        URI uri = URI.create(frontendOrigin);

        if (uri.getScheme() == null ||
            uri.getHost() == null ||
            uri.getUserInfo() != null ||
            uri.getPath() != null && !uri.getPath().isEmpty() ||
            uri.getQuery() != null ||
            uri.getFragment() != null ||
            !(uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {
            throw new IllegalArgumentException(
                "Invalid frontend origin"
            );
        }
    }
}
