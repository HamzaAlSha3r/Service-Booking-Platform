package com.testing.traningproject.security;

import com.testing.traningproject.model.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation for Spring Security
 * تطبيق مخصص لـ UserDetails للاستخدام مع Spring Security
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user; // Optional - can be null

    // Constructor with User entity
    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }

    // Constructor with minimal data (for JWT authentication)
    public CustomUserDetails(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.user = null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
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
        return user == null || user.getAccountStatus() != com.testing.traningproject.model.enums.AccountStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user == null || user.getAccountStatus() == com.testing.traningproject.model.enums.AccountStatus.ACTIVE;
    }

    public User getUser() {
        return user;
    }
}

