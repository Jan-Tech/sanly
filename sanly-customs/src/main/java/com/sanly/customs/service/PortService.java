package com.sanly.customs.service;

import com.sanly.customs.dto.request.CreatePortRequest;
import com.sanly.customs.dto.response.PortResponse;
import com.sanly.customs.entity.CustomsPort;
import com.sanly.customs.entity.PortStatus;
import com.sanly.customs.exception.RecordNotFoundException;
import com.sanly.customs.repository.CustomsPortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortService {
    private final CustomsPortRepository repo;

    @Transactional
    public PortResponse create(CreatePortRequest req) {
        int seq = repo.findMaxPortSequence() + 1;
        String code = "TM-PORT-%03d".formatted(seq);
        return PortResponse.from(repo.save(CustomsPort.builder()
                .portCode(code).name(req.name()).portType(req.portType())
                .region(req.region()).address(req.address()).status(PortStatus.ACTIVE).build()));
    }

    public List<PortResponse> findAll() {
        return repo.findAll().stream().map(PortResponse::from).toList();
    }

    public PortResponse findByCode(String portCode) {
        return repo.findByPortCode(portCode)
                .map(PortResponse::from)
                .orElseThrow(() -> new RecordNotFoundException("Port not found: " + portCode));
    }

    @Transactional
    public PortResponse updateStatus(String portCode, PortStatus status) {
        CustomsPort port = repo.findByPortCode(portCode)
                .orElseThrow(() -> new RecordNotFoundException("Port not found: " + portCode));
        port.setStatus(status); return PortResponse.from(repo.save(port));
    }
}
