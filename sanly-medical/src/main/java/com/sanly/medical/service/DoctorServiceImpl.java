package com.sanly.medical.service;

import com.sanly.medical.dto.request.DoctorCreateRequest;
import com.sanly.medical.dto.request.DoctorStatusRequest;
import com.sanly.medical.dto.response.DoctorResponse;
import com.sanly.medical.entity.Doctor;
import com.sanly.medical.exception.ClinicNotFoundException;
import com.sanly.medical.exception.DoctorNotFoundException;
import com.sanly.medical.exception.DuplicateLicenseException;
import com.sanly.medical.repository.ClinicRepository;
import com.sanly.medical.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository  doctorRepository;
    private final ClinicRepository  clinicRepository;
    private final PasswordEncoder   passwordEncoder;

    @Override
    @Transactional
    public DoctorResponse register(DoctorCreateRequest req) {
        if (doctorRepository.existsByUsername(req.getUsername())) {
            throw new DuplicateLicenseException("Doctor username", req.getUsername());
        }
        if (StringUtils.hasText(req.getLicenseNumber())
                && doctorRepository.existsByLicenseNumber(req.getLicenseNumber())) {
            throw new DuplicateLicenseException("Doctor license", req.getLicenseNumber());
        }
        if (StringUtils.hasText(req.getNationalId())
                && doctorRepository.existsByNationalId(req.getNationalId())) {
            throw new DuplicateLicenseException("Doctor national ID", req.getNationalId());
        }
        if (!clinicRepository.existsById(req.getClinicId())) {
            throw new ClinicNotFoundException(req.getClinicId());
        }

        Doctor doctor = Doctor.builder()
                .nationalId(req.getNationalId())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .specialization(req.getSpecialization())
                .licenseNumber(req.getLicenseNumber())
                .clinicId(req.getClinicId())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of("ROLE_DOCTOR"))
                .build();

        Doctor saved = doctorRepository.save(doctor);
        log.info("Registered doctor {} {} (id={}) at clinic {}",
                saved.getFirstName(), saved.getLastName(), saved.getDoctorId(), saved.getClinicId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> listAll() {
        return doctorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public DoctorResponse updateStatus(Long id, DoctorStatusRequest req) {
        Doctor doctor = findOrThrow(id);
        doctor.setStatus(req.getStatus());
        log.info("Doctor {} status changed to {}", id, req.getStatus());
        return toResponse(doctorRepository.save(doctor));
    }

    private Doctor findOrThrow(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
                .doctorId(d.getDoctorId())
                .nationalId(d.getNationalId())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .specialization(d.getSpecialization())
                .licenseNumber(d.getLicenseNumber())
                .clinicId(d.getClinicId())
                .username(d.getUsername())
                .roles(d.getRoles())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
