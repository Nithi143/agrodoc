package com.agrodoc.dto;

import java.util.List;

/**
 * Response DTO returned after a successful disease scan.
 * Contains all the information needed by result.html to render
 * the diagnosis, confidence meter, disease details, and remedies.
 */
public record ScanResultDto(

    /** Scan history record ID (for linking/history) */
    Long scanId,

    /** Disease label as returned by the AI API */
    String diseaseLabel,

    /** True if the leaf was classified as healthy */
    boolean isHealthy,

    /** AI confidence 0–100 (percentage, already multiplied) */
    double confidenceScore,

    /** Uploaded image filename (to display thumbnail) */
    String imageFilename,

    /** Timestamp string (ISO-8601) */
    String scannedAt,

    /** Full disease detail from the database (null if disease not found in DB) */
    DiseaseDetail diseaseDetail

) {
    /**
     * Nested DTO with full disease information from disease_info + disease_remedies tables.
     */
    public record DiseaseDetail(
        Long id,
        String diseaseName,
        String scientificName,
        String description,
        String symptoms,
        String causes,
        String preventionTips,
        String severityLevel,
        String imageUrl,
        List<RemedyDto> remedies
    ) {}

    /**
     * Individual remedy DTO.
     */
    public record RemedyDto(
        Long id,
        String remedyType,
        String remedyName,
        String application,
        String notes,
        int displayOrder
    ) {}
}
