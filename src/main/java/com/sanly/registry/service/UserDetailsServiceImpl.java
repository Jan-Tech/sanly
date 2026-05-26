package com.sanly.registry.service;

import com.sanly.registry.config.UserDetailsImpl;
import com.sanly.registry.entity.User;
import com.sanly.registry.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Citizens use nationalId as JWT subject; try username first, then nationalId
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByNationalId(username))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));
        return UserDetailsImpl.from(user);
    }
}
