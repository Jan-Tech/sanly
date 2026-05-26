package com.sanly.analytics.config;

import com.sanly.analytics.entity.AnalyticsOfficer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final AnalyticsOfficer officer;

    public UserDetailsImpl(AnalyticsOfficer officer) {
        this.officer = officer;
    }

    @Override public String getUsername() { return officer.getUsername(); }
    @Override public String getPassword() { return officer.getPasswordHash(); }
    @Override public boolean isEnabled() { return officer.isActive(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(officer.getRole()));
    }

    public AnalyticsOfficer getOfficer() { return officer; }
}
