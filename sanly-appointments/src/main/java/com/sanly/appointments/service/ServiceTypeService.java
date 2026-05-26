package com.sanly.appointments.service;

import com.sanly.appointments.dto.request.CreateServiceTypeRequest;
import com.sanly.appointments.entity.GovernmentOffice;
import com.sanly.appointments.entity.ServiceStatus;
import com.sanly.appointments.entity.ServiceType;
import com.sanly.appointments.repository.GovernmentOfficeRepository;
import com.sanly.appointments.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepo;
    private final GovernmentOfficeRepository officeRepo;

    @Transactional
    public ServiceType createServiceType(CreateServiceTypeRequest req) {
        GovernmentOffice office = officeRepo.findByOfficeCode(req.getOfficeCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Office not found."));
        ServiceType st = ServiceType.builder()
                .officeCode(req.getOfficeCode())
                .institutionType(office.getInstitutionType())
                .serviceName(req.getServiceName())
                .description(req.getDescription())
                .durationMinutes(req.getDurationMinutes() > 0 ? req.getDurationMinutes() : 30)
                .requiresDocuments(req.getRequiresDocuments())
                .status(ServiceStatus.ACTIVE)
                .build();
        return serviceTypeRepo.save(st);
    }

    @Transactional(readOnly = true)
    public List<ServiceType> getByOffice(String officeCode) {
        return serviceTypeRepo.findByOfficeCodeAndStatus(officeCode, ServiceStatus.ACTIVE);
    }

    @Transactional
    public ServiceType updateStatus(UUID serviceTypeId, ServiceStatus status) {
        ServiceType st = serviceTypeRepo.findById(serviceTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service type not found."));
        st.setStatus(status);
        return serviceTypeRepo.save(st);
    }
}
