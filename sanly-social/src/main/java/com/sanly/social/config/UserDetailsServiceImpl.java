package com.sanly.social.config;

import com.sanly.social.repository.SocialOfficerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final SocialOfficerRepository repo;
    public UserDetailsServiceImpl(SocialOfficerRepository repo) { this.repo = repo; }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username).map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));
    }
}
