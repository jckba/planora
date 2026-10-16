package com.planora.backend.common.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtDecoderConfiguration {

    @Bean
    public JwtDecoder jwtDecoder(
        RSAPublicKey verificationKey,
        JwtProperties properties
    ){
        NimbusJwtDecoder decoder =
            NimbusJwtDecoder.withPublicKey(verificationKey)
                .build();

        OAuth2TokenValidator<Jwt> validator =
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(
                    properties.issuer()
                ),
                new JwtSubjectValidator()
            );

        decoder.setJwtValidator(validator);

        return decoder;
    }
}
