package com.sanly.documents.repository;

import com.sanly.documents.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, UUID> {

    List<TrackingUpdate> findByTrackingCodeOrderByUpdatedAtAsc(String trackingCode);
}
