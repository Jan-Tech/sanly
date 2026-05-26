package com.sanly.documents.service;

import com.sanly.documents.client.NotificationClient;
import com.sanly.documents.dto.request.TrackingUpdateRequest;
import com.sanly.documents.dto.response.PageResponse;
import com.sanly.documents.dto.response.TrackedItemResponse;
import com.sanly.documents.dto.response.TrackingUpdateResponse;
import com.sanly.documents.entity.TrackedItem;
import com.sanly.documents.entity.TrackingUpdate;
import com.sanly.documents.repository.TrackedItemRepository;
import com.sanly.documents.repository.TrackingUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService {

    private final TrackedItemRepository trackedItemRepo;
    private final TrackingUpdateRepository trackingUpdateRepo;
    private final TrackingCodeService codeService;
    private final NotificationClient notificationClient;

    /**
     * Upsert a tracking item and append a new status update.
     * Called by other services via X-Service-Key authenticated endpoint.
     */
    @Transactional
    public TrackedItemResponse pushUpdate(TrackingUpdateRequest req) {
        TrackedItem item = trackedItemRepo.findBySourceItemCode(req.getSourceItemCode())
                .orElse(null);

        if (item == null) {
            // Create new tracked item
            item = new TrackedItem();
            item.setTrackingCode(codeService.generateCode());
            item.setCitizenNationalId(req.getCitizenNationalId());
            item.setItemType(req.getItemType());
            item.setSourceService(req.getSourceService());
            item.setSourceItemCode(req.getSourceItemCode());
            item.setTitle(req.getTitle());
            item.setLastUpdatedAt(LocalDateTime.now());
        } else {
            // Update existing
            item.setTitle(req.getTitle());
            item.setLastUpdatedAt(LocalDateTime.now());
        }

        item.setCurrentStatus(req.getCurrentStatus());
        item.setStatusDescription(req.getStatusDescription());
        item.setCompleted(req.isCompleted());

        if (req.isCompleted() && item.getCompletedAt() == null) {
            item.setCompletedAt(LocalDateTime.now());
        }
        if (!req.isCompleted()) {
            item.setCompletedAt(null);
        }

        item = trackedItemRepo.save(item);

        // Append tracking update record
        TrackingUpdate update = new TrackingUpdate();
        update.setTrackingCode(item.getTrackingCode());
        update.setStatus(req.getCurrentStatus());
        update.setDescription(req.getStatusDescription());
        update.setUpdatedByService(req.getSourceService().name());
        trackingUpdateRepo.save(update);

        // Async notifications
        try {
            notificationClient.send(item.getCitizenNationalId(),
                    "TRACKING_STATUS_UPDATED", "tk",
                    Map.of(
                            "trackingCode", item.getTrackingCode(),
                            "status", req.getCurrentStatus(),
                            "title", req.getTitle()
                    ));
        } catch (Exception e) {
            log.warn("TRACKING_STATUS_UPDATED notification failed: {}", e.getMessage());
        }

        if (req.isCompleted()) {
            try {
                notificationClient.send(item.getCitizenNationalId(),
                        "TRACKING_ITEM_COMPLETED", "tk",
                        Map.of(
                                "trackingCode", item.getTrackingCode(),
                                "title", req.getTitle()
                        ));
            } catch (Exception e) {
                log.warn("TRACKING_ITEM_COMPLETED notification failed: {}", e.getMessage());
            }
        }

        return toResponse(item, false);
    }

    /**
     * Returns all tracking items for a citizen, with optional completion filter.
     */
    @Transactional(readOnly = true)
    public List<TrackedItemResponse> getMyItems(String nationalId, Boolean isCompleted) {
        List<TrackedItem> items;
        if (isCompleted != null) {
            items = trackedItemRepo.findByCitizenNationalIdAndIsCompleted(nationalId, isCompleted);
        } else {
            items = trackedItemRepo.findByCitizenNationalIdOrderByLastUpdatedAtDesc(nationalId);
        }
        return items.stream()
                .map(i -> toResponse(i, false))
                .collect(Collectors.toList());
    }

    /**
     * Returns a single tracking item with full update timeline.
     */
    @Transactional(readOnly = true)
    public TrackedItemResponse getByCode(String trackingCode, String nationalId) {
        TrackedItem item = trackedItemRepo.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tracked item not found: " + trackingCode));
        if (!item.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this tracked item");
        }
        return toResponse(item, true);
    }

    /**
     * Finds a tracked item by source item code, checking citizen ownership.
     */
    @Transactional(readOnly = true)
    public TrackedItemResponse searchBySourceCode(String sourceCode, String nationalId) {
        TrackedItem item = trackedItemRepo.findBySourceItemCode(sourceCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No tracked item found for source code: " + sourceCode));
        if (!item.getCitizenNationalId().equals(nationalId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this tracked item");
        }
        return toResponse(item, true);
    }

    /**
     * Admin: paginated list of all tracked items.
     */
    @Transactional(readOnly = true)
    public PageResponse<TrackedItemResponse> adminGetAll(int page, int size) {
        Page<TrackedItem> pg = trackedItemRepo.findAllByOrderByLastUpdatedAtDesc(PageRequest.of(page, size));
        return PageResponse.<TrackedItemResponse>builder()
                .content(pg.getContent().stream()
                        .map(i -> toResponse(i, false))
                        .collect(Collectors.toList()))
                .page(pg.getNumber())
                .size(pg.getSize())
                .totalElements(pg.getTotalElements())
                .totalPages(pg.getTotalPages())
                .last(pg.isLast())
                .build();
    }

    // ---- Private helpers ----

    private TrackedItemResponse toResponse(TrackedItem item, boolean includeUpdates) {
        TrackedItemResponse.TrackedItemResponseBuilder builder = TrackedItemResponse.builder()
                .trackingId(item.getTrackingId())
                .trackingCode(item.getTrackingCode())
                .citizenNationalId(item.getCitizenNationalId())
                .itemType(item.getItemType())
                .sourceService(item.getSourceService())
                .sourceItemCode(item.getSourceItemCode())
                .title(item.getTitle())
                .currentStatus(item.getCurrentStatus())
                .statusDescription(item.getStatusDescription())
                .createdAt(item.getCreatedAt())
                .lastUpdatedAt(item.getLastUpdatedAt())
                .completedAt(item.getCompletedAt())
                .isCompleted(item.isCompleted());

        if (includeUpdates) {
            List<TrackingUpdateResponse> updates = trackingUpdateRepo
                    .findByTrackingCodeOrderByUpdatedAtAsc(item.getTrackingCode())
                    .stream()
                    .map(u -> TrackingUpdateResponse.builder()
                            .updateId(u.getUpdateId())
                            .status(u.getStatus())
                            .description(u.getDescription())
                            .updatedAt(u.getUpdatedAt())
                            .updatedByService(u.getUpdatedByService())
                            .build())
                    .collect(Collectors.toList());
            builder.updates(updates);
        }

        return builder.build();
    }
}
