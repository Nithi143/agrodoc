package com.agrodoc.repository;

import com.agrodoc.entity.ScanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the ScanHistory entity.
 */
@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    /**
     * Get paginated scan history for a specific user, newest first.
     */
    Page<ScanHistory> findByUserIdOrderByScannedAtDesc(Long userId, Pageable pageable);

    /**
     * Get recent scans for a user (used for dashboard).
     */
    List<ScanHistory> findTop3ByUserIdOrderByScannedAtDesc(Long userId);

    /**
     * Find a scan by ID, verifying it belongs to the requesting user.
     * Prevents users from accessing other users' scans.
     */
    Optional<ScanHistory> findByIdAndUserId(Long id, Long userId);

    /**
     * Count total scans for a user.
     */
    long countByUserId(Long userId);

    /**
     * Find the most commonly detected disease label for a user.
     */
    @Query("SELECT s.diseaseLabel, COUNT(s) as cnt FROM ScanHistory s " +
           "WHERE s.user.id = :userId AND s.isHealthy = false " +
           "GROUP BY s.diseaseLabel ORDER BY cnt DESC")
    List<Object[]> findMostCommonDiseaseByUserId(Long userId);
}
