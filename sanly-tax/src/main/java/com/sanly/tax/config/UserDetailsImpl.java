package com.sanly.tax.config;

import com.sanly.tax.entity.TaxOfficer;
import com.sanly.tax.entity.OfficerStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserDetailsImpl implements UserDetails {
    private final Long officerId;
    private final String username, password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long officerId, String username, String password,
                            boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.officerId = officerId; this.username = username;
        this.password = password; this.enabled = enabled; this.authorities = authorities;
    }

    public static UserDetailsImpl from(TaxOfficer o) {
        List<GrantedAuthority> auths = o.getRoles().stream()
                .map(SimpleGrantedAuthority::new).map(GrantedAuthority.class::cast).toList();
        return new UserDetailsImpl(o.getOfficerId(), o.getUsername(), o.getPassword(),
                o.getStatus() == OfficerStatus.ACTIVE, auths);
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
