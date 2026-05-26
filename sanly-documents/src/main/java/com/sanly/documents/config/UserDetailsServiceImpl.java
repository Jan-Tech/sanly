package com.sanly.documents.config;

import com.sanly.documents.repository.DocumentOfficerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DocumentOfficerRepository officerRepository;

    public UserDetailsServiceImpl(DocumentOfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return officerRepository.findByUsername(username)
                .map(UserDetailsImpl::from)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));
    }
}
