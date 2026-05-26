package com.sanly.police.service;

import com.sanly.police.config.UserDetailsImpl;
import com.sanly.police.entity.PoliceOfficer;
import com.sanly.police.repository.OfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final OfficerRepository officerRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PoliceOfficer officer = officerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return UserDetailsImpl.from(officer);
    }
}
