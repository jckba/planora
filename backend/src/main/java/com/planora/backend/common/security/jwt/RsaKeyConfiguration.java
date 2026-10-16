package com.planora.backend.common.security.jwt;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class RsaKeyConfiguration {

    private static final Pattern PEM_BOUNDARY =
        Pattern.compile("-----BEGIN [^-]+-----|-----END [^-]+-----");
    private final ResourceLoader resourceLoader;
    private final JwtProperties jwtProperties;

    public RsaKeyConfiguration(ResourceLoader resourceLoader, JwtProperties jwtProperties) {
        this.resourceLoader = resourceLoader;
        this.jwtProperties = jwtProperties;
    }

    @Bean
    RSAPrivateKey jwtSigningKey()
        throws IOException, GeneralSecurityException {

        byte[] encoded = readPem(jwtProperties.privateKeyLocation());

        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
            .generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    @Bean
    RSAPublicKey jwtVerificationKey()
        throws IOException, GeneralSecurityException {

        byte[] encoded = readPem(jwtProperties.publicKeyLocation());

        return (RSAPublicKey) KeyFactory.getInstance("RSA")
            .generatePublic(new X509EncodedKeySpec(encoded));
    }

    private byte[] readPem(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);

        if (!resource.exists()) {
            throw new IOException(
                "JWT key not found at: "
                    + resource.getDescription()
                    + " | Working directory: "
                    + System.getProperty("user.dir")
            );
        }

        String pem;
        try (var input = resource.getInputStream()) {
            pem = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        String base64 = PEM_BOUNDARY.matcher(pem)
            .replaceAll("")
            .replaceAll("\\s", "");

        return Base64.getDecoder().decode(base64);
    }

}
