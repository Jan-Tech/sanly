package com.sanly.banking.config;

import com.sanly.banking.entity.BankingOfficer;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class OfficerUserDetails implements UserDetails {

    private final BankingOfficer officer;

    public OfficerUserDetails(BankingOfficer officer) {
        this.officer = officer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(officer.getRole()));
    }

    @Override
    public String getPassword() {
        return officer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return officer.getUsername();
    }

    @Override
    public boolean isAccountNonExpired()    { return true; }

    @Override
    public boolean isAccountNonLocked()     { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled()              { return officer.isActive(); }
}
