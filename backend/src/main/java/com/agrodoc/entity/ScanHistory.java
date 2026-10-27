package com.agrodoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the `scan_history` table.
 * Records each disease detection scan performed by a user.
 * disease_id is nullable: if the AI returns an unrecognized label,
 * no FK is set but the raw label is still stored.
 */
@Entity
@Table(name = "scan_history")
@Getter
@Setter
@NoArgsConstructor
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = true)
    private DiseaseInfo diseaseInfo;

    @Column(name = "image_filename", nullable = false, length = 500)
    private String imageFilename;

    @Column(name = "disease_label", nullable = false, length = 150)
    private String diseaseLabel;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "is_healthy", nullable = false)
    private boolean isHealthy = false;

    @Column(name = "raw_api_result", columnDefinition = "JSON")
    private String rawApiResult;

    @Column(name = "scanned_at", nullable = false, updatable = false)
    private LocalDateTime scannedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (scannedAt == null) {
            scannedAt = LocalDateTime.now();
        }
    }
}
