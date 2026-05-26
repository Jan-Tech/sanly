package com.sanly.bridge.config;

import com.sanly.bridge.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long id, String username, String password,
                            boolean enabled,
                            Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.username    = username;
        this.password    = password;
        this.enabled     = enabled;
        this.authorities = authorities;
    }

    public static UserDetailsImpl from(User user) {
        List<GrantedAuthority> auths = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        return new UserDetailsImpl(user.getId(), user.getUsername(),
                user.getPassword(), user.isEnabled(), auths);
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
