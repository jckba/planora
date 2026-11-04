package com.planora.backend.common.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class JwtDecoderConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(
        RSAPublicKey verificationKey,
        JwtProperties properties
    ) {
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withPublicKey(verificationKey)
                .build();

        OAuth2TokenValidator<Jwt> validator =
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(
                    properties.issuer()
                ),
                new JwtSubjectValidator(),
                new JwtPurposeValidator()
            );

        decoder.setJwtValidator(validator);

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities =
                new ArrayList<>(
                    authoritiesConverter.convert(jwt)
                );
            String purpose = jwt.getClaimAsString("token_use");

            if (JwtTokenPurpose.ACCESS.name().equals(purpose)) {
                authorities.add(
                    new SimpleGrantedAuthority("TOKEN_ACCESS")
                );
            } else if (
                JwtTokenPurpose.ONBOARDING.name().equals(purpose)
            ) {
                authorities.add(
                    new SimpleGrantedAuthority("TOKEN_ONBOARDING")
                );
            }

            return authorities;
        });
        return converter;
    }

}
