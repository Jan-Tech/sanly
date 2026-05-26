package com.sanly.pharmacy.config;

import com.sanly.pharmacy.entity.PharmacyStaff;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long   staffId;
    private final String username;
    private final String password;
    private final String pharmacyCode;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(PharmacyStaff staff) {
        this.staffId      = staff.getStaffId();
        this.username     = staff.getUsername();
        this.password     = staff.getPassword();
        this.pharmacyCode = staff.getPharmacyCode();
        this.authorities  = List.of(new SimpleGrantedAuthority("ROLE_" + staff.getRole().name()));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
