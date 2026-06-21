package com.al.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String displayName;
    private final boolean enabled;
    private final List<String> roles;
    private final List<String> permissions;

    public LoginUser(Long userId, String username, String password, String displayName,
                     boolean enabled, List<String> roles, List<String> permissions) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.displayName = displayName;
        this.enabled = enabled;
        this.roles = roles;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Stream<SimpleGrantedAuthority> roleAuths = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        Stream<SimpleGrantedAuthority> permAuths = permissions.stream()
                .map(SimpleGrantedAuthority::new);
        return Stream.concat(roleAuths, permAuths).collect(Collectors.toList());
    }

    public boolean hasPermission(String permCode) {
        if (permissions == null) {
            return false;
        }
        return permissions.contains(permCode) || permissions.contains("*") || roles.contains("ADMIN");
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
        return enabled;
    }
}
