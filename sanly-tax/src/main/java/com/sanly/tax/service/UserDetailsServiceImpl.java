package com.sanly.tax.service;

import com.sanly.tax.config.UserDetailsImpl;
import com.sanly.tax.entity.TaxOfficer;
import com.sanly.tax.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final OfficerRepository officerRepository;

    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaxOfficer o = officerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return UserDetailsImpl.from(o);
    }
}
