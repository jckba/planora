package com.planora.backend.auth.security;

import com.planora.backend.auth.domain.User;
import com.planora.backend.auth.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanoraUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public PlanoraUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new PlanoraUserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("USER"))
        );
    }
}
