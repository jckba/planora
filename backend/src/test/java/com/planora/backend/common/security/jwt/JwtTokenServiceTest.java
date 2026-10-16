
package com.planora.backend.common.security.jwt;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String ISSUER = "http://localhost:8080";
    private KeyPair keyPair;
    private JwtProperties properties;
    private JwtTokenService tokenService;
    private JwtDecoder decoder;
    private Instant now;

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator generator =
            KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        properties = new JwtProperties(
            ISSUER,
            Duration.ofMinutes(15),
            "unused-private.pem",
            "unused-public.pem"
        );

        now = Instant.now()
            .truncatedTo(ChronoUnit.SECONDS);

        Clock clock = Clock.fixed(
            now,
            ZoneOffset.UTC
        );

        tokenService = new JwtTokenService(
            (RSAPrivateKey) keyPair.getPrivate(),
            properties,
            clock
        );

        decoder = new JwtDecoderConfiguration()
            .jwtDecoder(
                (RSAPublicKey) keyPair.getPublic(),
                properties
            );
    }

    @Test
    void shouldGenerateValidJwtWithExpectedClaims() {

        UUID userId = UUID.randomUUID();

        String token =
            tokenService.issueAccessToken(userId);

        Jwt jwt = decoder.decode(token);

        assertNotNull(token);
        assertEquals(userId.toString(), jwt.getSubject());
        assertEquals(ISSUER, Objects.requireNonNull(jwt.getIssuer()).toString());
        assertEquals(now, jwt.getIssuedAt());
        assertEquals(
            now.plus(Duration.ofMinutes(15)),
            jwt.getExpiresAt()
        );
        assertEquals("RS256", jwt.getHeaders().get("alg"));
    }

    @Test
    void shouldRejectExpiredJwt() {

        Clock pastClock = Clock.fixed(
            now.minus(Duration.ofHours(2)),
            ZoneOffset.UTC
        );

        JwtTokenService expiredTokenService =
            new JwtTokenService(
                (RSAPrivateKey) keyPair.getPrivate(),
                properties,
                pastClock
            );

        String token = expiredTokenService
            .issueAccessToken(UUID.randomUUID());

        assertThrows(
            JwtException.class,
            () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectJwtFromDifferentIssuer() {

        JwtProperties otherProperties =
            new JwtProperties(
                "https://another-issuer.example",
                Duration.ofMinutes(15),
                "unused-private.pem",
                "unused-public.pem"
            );

        JwtTokenService otherTokenService =
            new JwtTokenService(
                (RSAPrivateKey) keyPair.getPrivate(),
                otherProperties,
                Clock.fixed(now, ZoneOffset.UTC)
            );

        String token = otherTokenService
            .issueAccessToken(UUID.randomUUID());

        assertThrows(
            JwtException.class,
            () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectJwtSignedWithDifferentKey()
        throws Exception {

        KeyPairGenerator generator =
            KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair differentKeyPair =
            generator.generateKeyPair();

        JwtTokenService otherTokenService =
            new JwtTokenService(
                (RSAPrivateKey) differentKeyPair.getPrivate(),
                properties,
                Clock.fixed(now, ZoneOffset.UTC)
            );

        String token = otherTokenService
            .issueAccessToken(UUID.randomUUID());

        assertThrows(
            JwtException.class,
            () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectJwtWithInvalidSubject() {

        String token = Jwts.builder()
            .issuer(ISSUER)
            .subject("invalid-uuid")
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(now.plus(Duration.ofMinutes(15)))
            )
            .signWith(
                (RSAPrivateKey) keyPair.getPrivate(),
                Jwts.SIG.RS256
            )
            .compact();

        assertThrows(
            JwtException.class,
            () -> decoder.decode(token)
        );
    }

    @Test
    void shouldRejectJwtWithoutSubject() {

        String token = Jwts.builder()
            .issuer(ISSUER)
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(now.plus(Duration.ofMinutes(15)))
            )
            .signWith(
                (RSAPrivateKey) keyPair.getPrivate(),
                Jwts.SIG.RS256
            )
            .compact();

        assertThrows(
            JwtException.class,
            () -> decoder.decode(token)
        );
    }


}
