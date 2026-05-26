package com.sanly.court.service;

import com.sanly.court.dto.request.CreateCourtRequest;
import com.sanly.court.dto.response.CourtResponse;
import com.sanly.court.entity.Court;
import com.sanly.court.entity.CourtStatus;
import com.sanly.court.exception.RecordNotFoundException;
import com.sanly.court.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtManagementService {
    private final CourtRepository repo;

    @Transactional
    public CourtResponse create(CreateCourtRequest req) {
        int seq = repo.findMaxCourtSequence() + 1;
        return CourtResponse.from(repo.save(Court.builder()
                .courtCode("TM-CRT-%03d".formatted(seq))
                .name(req.name()).courtType(req.courtType())
                .region(req.region()).address(req.address())
                .status(CourtStatus.ACTIVE).build()));
    }

    public List<CourtResponse> findAll() {
        return repo.findAll().stream().map(CourtResponse::from).toList();
    }

    public CourtResponse findByCode(String courtCode) {
        return repo.findByCourtCode(courtCode)
                .map(CourtResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Court not found: " + courtCode));
    }

    @Transactional
    public CourtResponse updateStatus(String courtCode, CourtStatus status) {
        Court court = repo.findByCourtCode(courtCode)
                .orElseThrow(() -> new RecordNotFoundException("Court not found: " + courtCode));
        court.setStatus(status); return CourtResponse.from(repo.save(court));
    }
}
