package com.sanly.banking.config;

import com.sanly.banking.entity.BankingOfficer;
import com.sanly.banking.repository.BankingOfficerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OfficerUserDetailsService implements UserDetailsService {

    private final BankingOfficerRepository officerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        BankingOfficer officer = officerRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Officer not found: " + username));
        return new OfficerUserDetails(officer);
    }
}
