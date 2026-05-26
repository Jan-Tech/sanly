package com.sanly.social.config;

import com.sanly.social.entity.SocialOfficer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {
    private final SocialOfficer officer;
    public UserDetailsImpl(SocialOfficer o) { this.officer = o; }
    public UUID getOfficerId() { return officer.getOfficerId(); }
    public SocialOfficer getOfficer() { return officer; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + officer.getRole().name()));
    }
    @Override public String getPassword()               { return officer.getPassword(); }
    @Override public String getUsername()               { return officer.getUsername(); }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return officer.getStatus().name().equals("ACTIVE"); }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return officer.getStatus().name().equals("ACTIVE"); }
}
