package com.agrodoc.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO providing dashboard metrics for the authenticated user.
 * GET /api/users/me/stats
 */
public record UserStatsDto(
    long totalScans,
    String mostFrequentDisease,
    String mostCommonDisease,
    LocalDateTime lastScanDate,
    List<RecentScanDto> recentScans
) {
    /**
     * Preview representation of a recent scan for dashboard cards.
     */
    public record RecentScanDto(
        Long id,
        String diseaseLabel,
        BigDecimal confidenceScore,
        boolean isHealthy,
        String imageFilename,
        String imageUrl,
        LocalDateTime scannedAt
    ) {}
}

