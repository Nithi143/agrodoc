package com.agrodoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the `disease_info` table.
 * Contains canonical disease data. The disease_name must match
 * the label returned by the external AI API for database lookups to succeed.
 */
@Entity
@Table(name = "disease_info")
@Getter
@Setter
@NoArgsConstructor
public class DiseaseInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disease_name", nullable = false, unique = true, length = 100)
    private String diseaseName;

    @Column(name = "scientific_name", length = 150)
    private String scientificName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String symptoms;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String causes;

    @Column(name = "prevention_tips", columnDefinition = "TEXT")
    private String preventionTips;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_level", nullable = false, length = 10)
    private SeverityLevel severityLevel = SeverityLevel.MED;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "diseaseInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<DiseaseRemedy> remedies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum SeverityLevel {
        LOW, MED, HIGH
    }
}
