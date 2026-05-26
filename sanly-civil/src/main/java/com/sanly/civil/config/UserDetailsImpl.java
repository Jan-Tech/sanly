package com.sanly.civil.config;

import com.sanly.civil.entity.CivilOfficer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private final Long officerId;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long officerId, String username, String password,
                            Collection<? extends GrantedAuthority> authorities) {
        this.officerId = officerId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl from(CivilOfficer officer) {
        var authorities = officer.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UserDetailsImpl(officer.getId(), officer.getUsername(), officer.getPassword(), authorities);
    }

    public Long getOfficerId() { return officerId; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
