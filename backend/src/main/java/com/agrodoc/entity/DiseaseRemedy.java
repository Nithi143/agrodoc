package com.agrodoc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapped to the `disease_remedies` table.
 * Each row represents one organic or chemical remedy linked to a disease.
 */
@Entity
@Table(name = "disease_remedies")
@Getter
@Setter
@NoArgsConstructor
public class DiseaseRemedy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private DiseaseInfo diseaseInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "remedy_type", nullable = false, length = 20)
    private RemedyType remedyType;

    @Column(name = "remedy_name", nullable = false, length = 200)
    private String remedyName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String application;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 1;

    public enum RemedyType {
        ORGANIC, CHEMICAL
    }
}
