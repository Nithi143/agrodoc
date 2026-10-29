package com.agrodoc.service;

import com.agrodoc.dto.ScanResultDto;
import com.agrodoc.entity.DiseaseInfo;
import com.agrodoc.entity.ScanHistory;
import com.agrodoc.entity.User;
import com.agrodoc.repository.DiseaseInfoRepository;
import com.agrodoc.repository.ScanHistoryRepository;
import com.agrodoc.repository.UserRepository;
import com.agrodoc.service.PlantApiClient.ApiPrediction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core scan service orchestrating the full disease detection flow:
 * 1. Validates and saves the uploaded image file
 * 2. Sends image to external AI API via PlantApiClient
 * 3. Looks up disease details from the database
 * 4. Saves the scan result to scan_history
 * 5. Returns the ScanResultDto to the controller
 */
@Service
public class ScanService {

    private static final Logger log = LoggerFactory.getLogger(ScanService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PlantApiClient plantApiClient;
    private final DiseaseInfoRepository diseaseInfoRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ScanService(PlantApiClient plantApiClient,
                       DiseaseInfoRepository diseaseInfoRepository,
                       ScanHistoryRepository scanHistoryRepository,
                       UserRepository userRepository,
                       FileStorageService fileStorageService) {
        this.plantApiClient = plantApiClient;
        this.diseaseInfoRepository = diseaseInfoRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Perform a full disease scan:
     * upload image → call AI API → lookup DB → save history → return result.
     *
     * @param imageFile The uploaded leaf image
     * @param email     The authenticated user's email (from JWT)
     * @return ScanResultDto with all diagnosis and remedy data
     */
    @Transactional
    public ScanResultDto scan(MultipartFile imageFile, String email) {

        // 1. Load the authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 2. Save the uploaded image to the filesystem
        String savedFilename = fileStorageService.store(imageFile);

        // 3. Call the external AI API
        ApiPrediction prediction = plantApiClient.classify(imageFile);
        log.info("AI prediction for user {}: disease='{}' confidence={}% healthy={}",
                email, prediction.diseaseName(), prediction.confidence(), prediction.isHealthy());

        // 4. Look up disease info from database (may be null for unrecognized labels)
        Optional<DiseaseInfo> diseaseOpt = diseaseInfoRepository
                .findByDiseaseNameIgnoreCase(prediction.diseaseName());

        // 5. Save the scan history record
        ScanHistory history = new ScanHistory();
        history.setUser(user);
        history.setImageFilename(savedFilename);
        history.setDiseaseLabel(prediction.diseaseName());
        history.setConfidenceScore(BigDecimal.valueOf(prediction.confidence()));
        history.setHealthy(prediction.isHealthy());
        history.setRawApiResult(prediction.rawApiResponse());
        diseaseOpt.ifPresent(history::setDiseaseInfo);
        scanHistoryRepository.save(history);

        // 6. Build and return the response DTO
        ScanResultDto.DiseaseDetail diseaseDetail = diseaseOpt.map(this::toDiseaseDetail).orElse(null);

        String scannedAt = (history.getScannedAt() != null ? history.getScannedAt() : java.time.LocalDateTime.now()).format(FORMATTER);

        return new ScanResultDto(
                history.getId(),
                prediction.diseaseName(),
                prediction.isHealthy(),
                prediction.confidence(),
                savedFilename,
                scannedAt,
                diseaseDetail
        );
    }

    /**
     * Get a single scan result by ID for the authenticated user.
     * Enforces ownership: users can only access their own scans.
     */
    @Transactional(readOnly = true)
    public ScanResultDto getScanById(Long scanId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        ScanHistory history = scanHistoryRepository.findByIdAndUserId(scanId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Scan not found or access denied."));

        ScanResultDto.DiseaseDetail detail = history.getDiseaseInfo() != null
                ? toDiseaseDetail(history.getDiseaseInfo())
                : null;

        String scannedAt = (history.getScannedAt() != null ? history.getScannedAt() : java.time.LocalDateTime.now()).format(FORMATTER);

        return new ScanResultDto(
                history.getId(),
                history.getDiseaseLabel(),
                history.isHealthy(),
                history.getConfidenceScore() != null ? history.getConfidenceScore().doubleValue() : 0.0,
                history.getImageFilename(),
                scannedAt,
                detail
        );
    }

    // -------------------------------------------------------------------------

    private ScanResultDto.DiseaseDetail toDiseaseDetail(DiseaseInfo d) {
        List<ScanResultDto.RemedyDto> remedies = d.getRemedies().stream()
                .map(r -> new ScanResultDto.RemedyDto(
                        r.getId(),
                        r.getRemedyType().name(),
                        r.getRemedyName(),
                        r.getApplication(),
                        r.getNotes(),
                        r.getDisplayOrder()
                ))
                .collect(Collectors.toList());

        return new ScanResultDto.DiseaseDetail(
                d.getId(),
                d.getDiseaseName(),
                d.getScientificName(),
                d.getDescription(),
                d.getSymptoms(),
                d.getCauses(),
                d.getPreventionTips(),
                d.getSeverityLevel().name(),
                d.getImageUrl(),
                remedies
        );
    }
}
