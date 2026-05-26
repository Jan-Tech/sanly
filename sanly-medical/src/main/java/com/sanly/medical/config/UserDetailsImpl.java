package com.sanly.medical.config;

import com.sanly.medical.entity.Doctor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal. Carries {@code doctorId} and {@code clinicId}
 * so the service layer can enforce clinic-level record scoping without
 * additional DB lookups.
 */
@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long   doctorId;
    private final String username;
    private final String password;
    private final Long   clinicId;   // null for ROLE_ADMIN accounts
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserDetailsImpl(Long doctorId, String username, String password,
                            Long clinicId, boolean enabled,
                            Collection<? extends GrantedAuthority> authorities) {
        this.doctorId    = doctorId;
        this.username    = username;
        this.password    = password;
        this.clinicId    = clinicId;
        this.enabled     = enabled;
        this.authorities = authorities;
    }

    public static UserDetailsImpl from(Doctor doctor) {
        List<GrantedAuthority> auths = doctor.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        return new UserDetailsImpl(
                doctor.getDoctorId(), doctor.getUsername(), doctor.getPassword(),
                doctor.getClinicId(),
                doctor.getStatus() == com.sanly.medical.entity.DoctorStatus.ACTIVE,
                auths);
    }

    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
}
