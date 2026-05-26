package com.sanly.land.config;

import com.sanly.land.entity.LandOfficer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {
    private final LandOfficer officer;

    public UserDetailsImpl(LandOfficer officer) { this.officer = officer; }

    public UUID getOfficerId() { return officer.getOfficerId(); }
    public String getRegion()  { return officer.getRegion(); }
    public LandOfficer getOfficer() { return officer; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + officer.getRole().name()));
    }

    @Override public String getPassword()               { return officer.getPassword(); }
    @Override public String getUsername()               { return officer.getUsername(); }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return officer.getStatus().name().equals("ACTIVE"); }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return officer.getStatus().name().equals("ACTIVE"); }
}
