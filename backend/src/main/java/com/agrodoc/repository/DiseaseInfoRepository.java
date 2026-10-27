package com.agrodoc.repository;

import com.agrodoc.entity.DiseaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the DiseaseInfo entity.
 */
@Repository
public interface DiseaseInfoRepository extends JpaRepository<DiseaseInfo, Long> {

    /**
     * Lookup disease by exact name — case-insensitive.
     * The disease_name in the DB must match what the AI API returns.
     */
    Optional<DiseaseInfo> findByDiseaseNameIgnoreCase(String diseaseName);

    /**
     * Search diseases by name or description keyword (for library search).
     */
    @Query("SELECT d FROM DiseaseInfo d WHERE LOWER(d.diseaseName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiseaseInfo> searchByKeyword(String keyword);
}
