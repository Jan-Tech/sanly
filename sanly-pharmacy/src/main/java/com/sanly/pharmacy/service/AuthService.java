package com.sanly.pharmacy.service;

import com.sanly.pharmacy.config.JwtTokenProvider;
import com.sanly.pharmacy.dto.request.LoginRequest;
import com.sanly.pharmacy.dto.response.LoginResponse;
import com.sanly.pharmacy.entity.PharmacyStaff;
import com.sanly.pharmacy.entity.StaffStatus;
import com.sanly.pharmacy.repository.PharmacyStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PharmacyStaffRepository staffRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtTokenProvider        jwtTokenProvider;

    public LoginResponse login(LoginRequest req) {
        PharmacyStaff staff = staffRepository.findByUsername(req.getUsername())
                .filter(s -> s.getStatus() == StaffStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), staff.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtTokenProvider.generate(staff);
        return new LoginResponse(token, staff.getRole().name(), staff.getPharmacyCode());
    }
}
