package com.sanly.pension.config;

import com.sanly.pension.entity.PensionOfficer;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {
    private final Long officerId;
    private final String username;
    private final String password;
    private final String role;
    private final String employerCode;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(PensionOfficer officer) {
        this.officerId    = officer.getOfficerId();
        this.username     = officer.getUsername();
        this.password     = officer.getPassword();
        this.role         = officer.getRole().name();
        this.employerCode = officer.getEmployerCode();
        this.authorities  = List.of(new SimpleGrantedAuthority("ROLE_" + officer.getRole().name()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
