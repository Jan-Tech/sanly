package com.sanly.bridge.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Spring Security token set by {@link InstitutionAuthFilter} after a
 * successful X-Institution-Key validation.
 *
 * Principal = institutionCode (String).
 * Authority = ROLE_INSTITUTION.
 */
public class InstitutionAuthentication extends AbstractAuthenticationToken {

    private final String institutionCode;

    public InstitutionAuthentication(String institutionCode) {
        super(List.of(new SimpleGrantedAuthority("ROLE_INSTITUTION")));
        this.institutionCode = institutionCode;
        super.setAuthenticated(true);
    }

    @Override public Object getCredentials()  { return null; }
    @Override public Object getPrincipal()    { return institutionCode; }
    @Override public String getName()         { return institutionCode; }
}
