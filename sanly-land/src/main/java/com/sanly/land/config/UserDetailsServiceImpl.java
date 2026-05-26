package com.sanly.land.config;

import com.sanly.land.repository.LandOfficerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final LandOfficerRepository officerRepository;

    public UserDetailsServiceImpl(LandOfficerRepository r) { this.officerRepository = r; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return officerRepository.findByUsername(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));
    }
}
