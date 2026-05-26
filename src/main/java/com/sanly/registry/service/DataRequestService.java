package com.sanly.registry.service;

import com.sanly.registry.client.NotificationClient;
import com.sanly.registry.dto.request.DataRequestSubmitRequest;
import com.sanly.registry.dto.response.DataRequestResponse;
import com.sanly.registry.dto.response.PageResponse;
import com.sanly.registry.entity.AffectedService;
import com.sanly.registry.entity.DataDeletionRequest;
import com.sanly.registry.entity.RequestStatus;
import com.sanly.registry.entity.RequestType;
import com.sanly.registry.repository.CitizenRepository;
import com.sanly.registry.repository.DataDeletionRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataRequestService {

    private final DataDeletionRequestRepository dataRequestRepo;
    private final DdrCodeService ddrCodeService;
    private final DataExportService dataExportService;
    private final NotificationClient notificationClient;
    private final CitizenRepository citizenRepository;

    @Transactional
    public DataRequestResponse submit(String nationalId, DataRequestSubmitRequest req) {
        long requestsThisMonth = dataRequestRepo.countByCitizenNationalIdAndSubmittedAtAfter(
                nationalId, LocalDate.now().withDayOfMonth(1).atStartOfDay());
        if (requestsThisMonth >= 3) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "You have reached the maximum of 3 data requests per month.");
        }

        String requestCode = ddrCodeService.generateCode();

        DataDeletionRequest request = DataDeletionRequest.builder()
                .requestCode(requestCode)
                .citizenNationalId(nationalId)
                .requestType(req.getRequestType())
                .affectedService(req.getAffectedService())
                .description(req.getDescription())
                .status(RequestStatus.PENDING)
                .build();

        DataDeletionRequest saved = dataRequestRepo.save(request);

        notificationClient.send(nationalId, "DATA_REQUEST_RECEIVED", "EN",
                Map.of("requestCode", requestCode, "requestType", req.getRequestType().name()));

        log.info("[DDR] Submitted request={} for nationalId={}", requestCode, nationalId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<DataRequestResponse> getMyRequests(String nationalId, int page, int size) {
        Page<DataDeletionRequest> resultPage = dataRequestRepo
                .findByCitizenNationalIdOrderBySubmittedAtDesc(nationalId, PageRequest.of(page, size));
        return toPageResponse(resultPage);
    }

    @Transactional(readOnly = true)
    public DataRequestResponse getMyRequest(String nationalId, String requestCode) {
        DataDeletionRequest request = dataRequestRepo.findByRequestCode(requestCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Data request not found: " + requestCode));
        if (!request.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this request.");
        }
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<DataRequestResponse> adminGetAll(RequestStatus status, RequestType type,
                                                          AffectedService service, int page, int size) {
        Page<DataDeletionRequest> resultPage = dataRequestRepo.findWithFilters(
                status, type, service, PageRequest.of(page, size));
        return toPageResponse(resultPage);
    }

    @Transactional(readOnly = true)
    public DataRequestResponse adminGetByCode(String requestCode) {
        return toResponse(findByCodeOrThrow(requestCode));
    }

    @Transactional
    public DataRequestResponse startReview(String requestCode, String officerId) {
        DataDeletionRequest request = findByCodeOrThrow(requestCode);
        request.setStatus(RequestStatus.UNDER_REVIEW);
        request.setReviewedByOfficerId(officerId);
        return toResponse(dataRequestRepo.save(request));
    }

    @Transactional
    public DataRequestResponse approve(String requestCode, String officerId, String notes) {
        DataDeletionRequest request = findByCodeOrThrow(requestCode);

        if (request.getRequestType() == RequestType.EXPORT_MY_DATA) {
            String exportJson = dataExportService.compileExport(request.getCitizenNationalId());
            request.setExportData(exportJson);
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByOfficerId(officerId);
        request.setResolutionDescription(notes);

        DataDeletionRequest saved = dataRequestRepo.save(request);

        notificationClient.send(request.getCitizenNationalId(), "DATA_REQUEST_APPROVED", "EN",
                Map.of("requestCode", requestCode));

        log.info("[DDR] Approved request={} by officerId={}", requestCode, officerId);
        return toResponse(saved);
    }

    @Transactional
    public DataRequestResponse reject(String requestCode, String officerId, String notes) {
        DataDeletionRequest request = findByCodeOrThrow(requestCode);
        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByOfficerId(officerId);
        request.setReviewerNotes(notes);

        DataDeletionRequest saved = dataRequestRepo.save(request);

        notificationClient.send(request.getCitizenNationalId(), "DATA_REQUEST_REJECTED", "EN",
                Map.of("requestCode", requestCode));

        log.info("[DDR] Rejected request={} by officerId={}", requestCode, officerId);
        return toResponse(saved);
    }

    @Transactional
    public DataRequestResponse partialApprove(String requestCode, String officerId, String notes) {
        DataDeletionRequest request = findByCodeOrThrow(requestCode);
        request.setStatus(RequestStatus.PARTIALLY_APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedByOfficerId(officerId);
        request.setResolutionDescription(notes);

        DataDeletionRequest saved = dataRequestRepo.save(request);

        notificationClient.send(request.getCitizenNationalId(), "DATA_REQUEST_PARTIAL", "EN",
                Map.of("requestCode", requestCode));

        log.info("[DDR] Partially approved request={} by officerId={}", requestCode, officerId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public String getExportData(String nationalId, String requestCode) {
        DataDeletionRequest request = dataRequestRepo.findByRequestCode(requestCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Data request not found: " + requestCode));

        if (!request.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You do not have access to this request.");
        }

        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Export is only available for APPROVED requests.");
        }

        if (request.getRequestType() != RequestType.EXPORT_MY_DATA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This request is not of type EXPORT_MY_DATA.");
        }

        if (request.getExportData() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Export data is not yet available for this request.");
        }

        return request.getExportData();
    }

    // ---- private helpers ----

    private DataDeletionRequest findByCodeOrThrow(String requestCode) {
        return dataRequestRepo.findByRequestCode(requestCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Data request not found: " + requestCode));
    }

    private DataRequestResponse toResponse(DataDeletionRequest r) {
        return DataRequestResponse.toResponse(r);
    }

    private PageResponse<DataRequestResponse> toPageResponse(Page<DataDeletionRequest> page) {
        return PageResponse.<DataRequestResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
