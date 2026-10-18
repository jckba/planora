package com.planora.backend.auth.security;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NullMarked
public class PlanoraUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PlanoraUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        String value = identifier.trim();

        User user = (value.contains("@")
            ? userRepository.findByEmail(value)
            : userRepository.findByUsername(value))
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Invalid credentials"
                )
            );

        String passwordHash = user.getPasswordHash();

        if (passwordHash == null) {
            throw new UsernameNotFoundException("Invalid credentials");
        }


        return new PlanoraUserPrincipal(
            user.getId(),
            user.getEmail(),
            passwordHash,
            List.of(new SimpleGrantedAuthority("USER"))
        );
    }
}
