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

        // 4. Look up disease info from database (normalizing label if needed)
        Optional<DiseaseInfo> diseaseOpt = resolveDiseaseInfo(prediction.diseaseName());

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

        DiseaseInfo diseaseInfo = history.getDiseaseInfo();
        if (diseaseInfo == null && history.getDiseaseLabel() != null && !history.isHealthy()) {
            diseaseInfo = resolveDiseaseInfo(history.getDiseaseLabel()).orElse(null);
        }

        ScanResultDto.DiseaseDetail detail = diseaseInfo != null
                ? toDiseaseDetail(diseaseInfo)
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

    /**
     * Resolves DiseaseInfo from the database by normalizing the AI prediction label.
     * Preserves exact matches first to ensure existing disease lookups remain intact,
     * then checks canonical aliases and prefix variations ("Tomato ").
     *
     * @param rawLabel Disease label returned by the AI classifier
     * @return Optional containing the matched DiseaseInfo, or empty if unrecognized
     */
    public Optional<DiseaseInfo> resolveDiseaseInfo(String rawLabel) {
        if (rawLabel == null || rawLabel.isBlank()) {
            return Optional.empty();
        }

        String trimmed = rawLabel.trim();

        // 1. Direct match (case-insensitive) — preserves existing exact database mappings
        Optional<DiseaseInfo> directMatch = diseaseInfoRepository.findByDiseaseNameIgnoreCase(trimmed);
        if (directMatch.isPresent()) {
            return directMatch;
        }

        // 2. Canonical mapping for specific external AI terminology variations
        String canonical = mapToCanonicalName(trimmed);
        if (canonical != null && !canonical.equalsIgnoreCase(trimmed)) {
            Optional<DiseaseInfo> canonicalMatch = diseaseInfoRepository.findByDiseaseNameIgnoreCase(canonical);
            if (canonicalMatch.isPresent()) {
                return canonicalMatch;
            }
        }

        // 3. Try with "Tomato " prefix if absent (e.g. "Early Blight" -> "Tomato Early Blight")
        if (!trimmed.toLowerCase().startsWith("tomato ")) {
            Optional<DiseaseInfo> tomatoPrefixed = diseaseInfoRepository.findByDiseaseNameIgnoreCase("Tomato " + trimmed);
            if (tomatoPrefixed.isPresent()) {
                return tomatoPrefixed;
            }
        } else {
            // 4. Try stripping "Tomato " prefix (e.g. "Tomato Nutrient Deficiency" -> "Nutrient Deficiency")
            String stripped = trimmed.substring(7).trim();
            if (!stripped.isEmpty()) {
                Optional<DiseaseInfo> strippedMatch = diseaseInfoRepository.findByDiseaseNameIgnoreCase(stripped);
                if (strippedMatch.isPresent()) {
                    return strippedMatch;
                }
            }
        }

        return Optional.empty();
    }

    private String mapToCanonicalName(String label) {
        String lower = label.toLowerCase();
        return switch (lower) {
            case "two-spotted spider mite", "two-spotted spider mites", "spider mite", "spider mites" ->
                    "Tomato Spider Mites";
            case "yellow leaf curl virus", "tomato yellow leaf curl" ->
                    "Tomato Yellow Leaf Curl Virus";
            case "mosaic virus", "tobacco mosaic virus" ->
                    "Tomato Mosaic Virus";
            case "bacterial spot" ->
                    "Tomato Bacterial Spot";
            case "early blight" ->
                    "Tomato Early Blight";
            case "late blight" ->
                    "Tomato Late Blight";
            case "leaf mold" ->
                    "Tomato Leaf Mold";
            case "septoria leaf spot" ->
                    "Tomato Septoria Leaf Spot";
            case "target spot" ->
                    "Tomato Target Spot";
            case "nutrient deficiency" ->
                    "Nutrient Deficiency";
            case "healthy" ->
                    "Tomato Healthy";
            default -> null;
        };
    }
}
