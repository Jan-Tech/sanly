package com.sanly.civil.service;

import com.sanly.civil.config.UserDetailsImpl;
import com.sanly.civil.repository.OfficerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final OfficerRepository officerRepository;

    public UserDetailsServiceImpl(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return officerRepository.findByUsername(username)
                .map(UserDetailsImpl::from)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));
    }
}
