
package com.planora.backend.auth.security;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanoraUserDetailsServiceTest {

    private UserRepository userRepository;
    private PlanoraUserDetailsService userDetailsService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);

        userDetailsService =
            new PlanoraUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByEmail() {

        User user = createMockUser();

        when(userRepository.findByEmail("jckba@example.com"))
            .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService
            .loadUserByUsername("jckba@example.com");

        PlanoraUserPrincipal principal =
            assertInstanceOf(
                PlanoraUserPrincipal.class,
                result
            );

        assertEquals(userId, principal.getUserId());

        verify(userRepository)
            .findByEmail("jckba@example.com");

        verify(userRepository, never())
            .findByUsername(anyString());
    }

    @Test
    void shouldLoadUserByUsername() {

        User user = createMockUser();

        when(userRepository.findByUsername("jckba"))
            .thenReturn(Optional.of(user));

        UserDetails result = userDetailsService
            .loadUserByUsername("jckba");

        PlanoraUserPrincipal principal =
            assertInstanceOf(
                PlanoraUserPrincipal.class,
                result
            );

        assertEquals(userId, principal.getUserId());

        verify(userRepository)
            .findByUsername("jckba");

        verify(userRepository, never())
            .findByEmail(anyString());
    }

    @Test
    void shouldRejectUnknownEmail() {

        when(userRepository.findByEmail("unknown@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(
                "unknown@example.com"
            )
        );
    }

    @Test
    void shouldRejectUnknownUsername() {

        when(userRepository.findByUsername("unknown"))
            .thenReturn(Optional.empty());

        assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(
                "unknown"
            )
        );
    }

    @Test
    void shouldTrimIdentifier() {

        User user = createMockUser();

        when(userRepository.findByUsername("jckba"))
            .thenReturn(Optional.of(user));

        userDetailsService.loadUserByUsername("  jckba  ");

        verify(userRepository).findByUsername("jckba");
    }

    private User createMockUser() {

        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("jckba@example.com");
        when(user.getPasswordHash())
            .thenReturn("{bcrypt}test-hash");

        return user;
    }
}
