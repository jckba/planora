package com.planora.backend.auth.security;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class PlanoraUserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String password;
    private final boolean emailVerified;
    private final boolean profileComplete;
    private final Collection<? extends GrantedAuthority> authorities;

    public PlanoraUserPrincipal(UUID userId, String email, String password, boolean emailVerified, boolean profileComplete, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.emailVerified = emailVerified;
        this.profileComplete = profileComplete;
        this.authorities = List.copyOf(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return emailVerified;
    }
}
