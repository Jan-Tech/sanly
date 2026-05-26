package com.sanly.registry.repository;

import com.sanly.registry.entity.NinSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface NinSequenceRepository extends JpaRepository<NinSequence, String> {

    /**
     * Acquires a PostgreSQL row-level write lock (SELECT FOR UPDATE) on the
     * sequence row for {@code prefix}. Must be called inside an active transaction.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM NinSequence s WHERE s.prefix = :prefix")
    Optional<NinSequence> findByPrefixWithLock(@Param("prefix") String prefix);

    /**
     * Idempotent seed — inserts a sequence row only if the prefix is new.
     * ON CONFLICT DO NOTHING prevents duplicate-key errors under concurrent
     * registrations for the same birth-date/gender prefix.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO nin_sequences (prefix, last_sequence)
        VALUES (:prefix, 0)
        ON CONFLICT (prefix) DO NOTHING
        """, nativeQuery = true)
    void insertIfNotExists(@Param("prefix") String prefix);
}
