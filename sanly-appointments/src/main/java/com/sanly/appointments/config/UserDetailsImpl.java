package com.sanly.appointments.config;

import com.sanly.appointments.entity.AppointmentOfficer;
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
    private final String officeCode;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password,
                           String officeCode, String role,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.officeCode = officeCode;
        this.role = role;
        this.authorities = authorities;
    }

    public static UserDetailsImpl from(AppointmentOfficer officer) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + officer.getRole().name())
        );
        return new UserDetailsImpl(
                officer.getOfficerId(),
                officer.getUsername(),
                officer.getPassword(),
                officer.getOfficeCode(),
                officer.getRole().name(),
                authorities
        );
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
        return true;
    }
}
