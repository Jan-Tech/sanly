package com.sanly.registry.service;

import com.sanly.registry.config.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * RBAC helper exposed as a Spring bean so it can be referenced in
 * {@code @PreAuthorize} SpEL expressions as {@code @securityService}.
 */
@Service("securityService")
public class SecurityService {

    /**
     * Returns {@code true} if the currently authenticated user is a CITIZEN
     * whose linked national ID matches {@code nationalId}.
     * Used to allow citizens to access only their own record.
     */
    public boolean isOwnRecord(String nationalId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        if (!(auth.getPrincipal() instanceof UserDetailsImpl principal)) return false;
        return nationalId != null && nationalId.equals(principal.getNationalId());
    }
}
