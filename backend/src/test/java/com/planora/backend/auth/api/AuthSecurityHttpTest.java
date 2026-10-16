
package com.planora.backend.auth.api;

import com.jayway.jsonpath.JsonPath;
import com.planora.backend.auth.application.user.CreateUserUseCase;
import com.planora.backend.auth.security.PlanoraUserPrincipal;
import com.planora.backend.common.security.SecurityConfig;
import com.planora.backend.common.security.SecurityCurrentUser;
import com.planora.backend.common.security.jwt.JwtDecoderConfiguration;
import com.planora.backend.common.security.jwt.JwtProperties;
import com.planora.backend.common.security.jwt.JwtTokenService;

import io.jsonwebtoken.Jwts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({
    SecurityConfig.class,
    SecurityCurrentUser.class,
    JwtDecoderConfiguration.class,
    JwtTokenService.class,
    AuthSecurityHttpTest.TestJwtConfiguration.class
})
class AuthSecurityHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService tokenService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private RSAPrivateKey signingKey;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private Clock clock;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void shouldReturn401WithoutJwt() throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
            )
            .andExpect(status().isUnauthorized())
            .andExpect(header().exists(
                HttpHeaders.WWW_AUTHENTICATE
            ));
    }

    @Test
    void shouldReturn200WithValidJwt() throws Exception {

        UUID userId = UUID.randomUUID();

        String token = tokenService.issueAccessToken(userId);

        mockMvc.perform(
                get("/api/auth/me")
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                    )
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId")
                .value(userId.toString()));
    }

    @Test
    void shouldReturn401WithInvalidJwt() throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer invalid.token.value"
                    )
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WithInvalidSubject() throws Exception {

        Instant now = clock.instant();

        String token = Jwts.builder()
            .issuer(jwtProperties.issuer())
            .subject("invalid-uuid")
            .issuedAt(Date.from(now))
            .expiration(
                Date.from(now.plus(
                    jwtProperties.accessTokenTtl()
                ))
            )
            .signWith(signingKey, Jwts.SIG.RS256)
            .compact();

        mockMvc.perform(
                get("/api/auth/me")
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                    )
            )
            .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldLoginWithEmailAndReturnJwt() throws Exception {
        assertSuccessfulLogin("jckba@example.com");
    }

    @Test
    void shouldLoginWithUsernameAndReturnJwt() throws Exception {
        assertSuccessfulLogin("jckba");
    }

    @Test
    void shouldRejectInvalidPassword() throws Exception {

        configureAuthenticatedUser("jckba@example.com");

        mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                      "identifier": "jckba@example.com",
                      "password": "incorrect-password"
                    }
                    """)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownUser() throws Exception {

        when(userDetailsService.loadUserByUsername("unknown"))
            .thenThrow(
                new org.springframework.security.core.userdetails
                    .UsernameNotFoundException("Invalid credentials")
            );

        mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                    {
                      "identifier": "unknown",
                      "password": "correct-password"
                    }
                    """)
            )
            .andExpect(status().isUnauthorized());
    }

    private void assertSuccessfulLogin(
        String identifier
    ) throws Exception {

        UUID userId = configureAuthenticatedUser(identifier);

        String body = """
        {
          "identifier": "%s",
          "password": "correct-password"
        }
        """.formatted(identifier);

        String response = mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900))
            .andReturn()
            .getResponse()
            .getContentAsString();

        String accessToken =
            JsonPath.read(response, "$.accessToken");

        assertEquals(
            userId.toString(),
            jwtDecoder.decode(accessToken).getSubject()
        );
    }

    private UUID configureAuthenticatedUser(
        String identifier
    ) {

        UUID userId = UUID.randomUUID();

        PlanoraUserPrincipal principal =
            new PlanoraUserPrincipal(
                userId,
                "jckba@example.com",
                passwordEncoder.encode("correct-password"),
                List.of(
                    new SimpleGrantedAuthority("USER")
                )
            );

        when(userDetailsService.loadUserByUsername(identifier))
            .thenReturn(principal);

        return userId;
    }



    @TestConfiguration(proxyBeanMethods = false)
    static class TestJwtConfiguration {

        @Bean
        KeyPair testKeyPair() throws Exception {

            KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            return generator.generateKeyPair();
        }

        @Bean
        RSAPrivateKey signingKey(KeyPair keyPair) {
            return (RSAPrivateKey) keyPair.getPrivate();
        }

        @Bean
        RSAPublicKey verificationKey(KeyPair keyPair) {
            return (RSAPublicKey) keyPair.getPublic();
        }

        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties(
                "http://localhost:8080",
                Duration.ofMinutes(15),
                "unused-private.pem",
                "unused-public.pem"
            );
        }

        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }
}
