package com.agrodoc.service;

import com.agrodoc.dto.ScanResultDto;
import com.agrodoc.entity.DiseaseInfo;
import com.agrodoc.entity.DiseaseRemedy;
import com.agrodoc.entity.ScanHistory;
import com.agrodoc.entity.User;
import com.agrodoc.repository.DiseaseInfoRepository;
import com.agrodoc.repository.ScanHistoryRepository;
import com.agrodoc.repository.UserRepository;
import com.agrodoc.service.PlantApiClient.ApiPrediction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private PlantApiClient plantApiClient;

    @Mock
    private DiseaseInfoRepository diseaseInfoRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ScanService scanService;

    private User testUser;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("farmer@example.com");
        testUser.setFullName("Tomato Farmer");

        mockFile = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );
    }

    @Test
    void shouldSuccessfullyScanDiseasedLeafAndSaveHistory() {
        // Arrange
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(mockFile)).thenReturn("stored-uuid-123.jpg");

        ApiPrediction prediction = new ApiPrediction(
                "Early Blight", 95.5, false, "{\"mock\": true}"
        );
        when(plantApiClient.classify(mockFile)).thenReturn(prediction);

        DiseaseInfo disease = new DiseaseInfo();
        disease.setId(10L);
        disease.setDiseaseName("Early Blight");
        disease.setScientificName("Alternaria solani");
        disease.setDescription("Fungal disease affecting tomato foliage.");
        disease.setSymptoms("Concentric rings on lower leaves.");
        disease.setCauses("High humidity and fungal spores.");
        disease.setPreventionTips("Water at base, space plants.");
        disease.setSeverityLevel(DiseaseInfo.SeverityLevel.HIGH);

        DiseaseRemedy remedy = new DiseaseRemedy();
        remedy.setId(100L);
        remedy.setRemedyType(DiseaseRemedy.RemedyType.ORGANIC);
        remedy.setRemedyName("Copper Fungicide");
        remedy.setApplication("Apply every 7 days.");
        disease.getRemedies().add(remedy);

        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase("Early Blight"))
                .thenReturn(Optional.of(disease));

        // Act
        ScanResultDto result = scanService.scan(mockFile, "farmer@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Early Blight", result.diseaseLabel());
        assertFalse(result.isHealthy());
        assertEquals(95.5, result.confidenceScore());
        assertEquals("stored-uuid-123.jpg", result.imageFilename());
        assertNotNull(result.diseaseDetail());
        assertEquals("Early Blight", result.diseaseDetail().diseaseName());
        assertEquals("HIGH", result.diseaseDetail().severityLevel());
        assertEquals(1, result.diseaseDetail().remedies().size());

        // Verify history was saved to DB
        ArgumentCaptor<ScanHistory> historyCaptor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository, times(1)).save(historyCaptor.capture());
        ScanHistory saved = historyCaptor.getValue();
        assertEquals(testUser, saved.getUser());
        assertEquals("stored-uuid-123.jpg", saved.getImageFilename());
        assertEquals("Early Blight", saved.getDiseaseLabel());
        assertEquals(BigDecimal.valueOf(95.5), saved.getConfidenceScore());
        assertFalse(saved.isHealthy());
        assertEquals(disease, saved.getDiseaseInfo());
    }

    @Test
    void shouldSuccessfullyScanHealthyLeaf() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(mockFile)).thenReturn("healthy-uuid.jpg");

        ApiPrediction prediction = new ApiPrediction(
                "Healthy", 99.0, true, "{\"healthy\": true}"
        );
        when(plantApiClient.classify(mockFile)).thenReturn(prediction);
        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase("Healthy")).thenReturn(Optional.empty());

        ScanResultDto result = scanService.scan(mockFile, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Healthy", result.diseaseLabel());
        assertTrue(result.isHealthy());
        assertEquals(99.0, result.confidenceScore());
        assertNull(result.diseaseDetail());

        verify(scanHistoryRepository, times(1)).save(any(ScanHistory.class));
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringScan() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                scanService.scan(mockFile, "unknown@example.com"));

        verify(plantApiClient, never()).classify(any());
    }

    @Test
    void shouldGetScanByIdEnforcingOwnership() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));

        ScanHistory history = new ScanHistory();
        history.setUser(testUser);
        history.setDiseaseLabel("Late Blight");
        history.setImageFilename("uuid-test.jpg");
        history.setConfidenceScore(BigDecimal.valueOf(92.0));
        history.setHealthy(false);

        // Manually set scannedAt if not persisted through JPA
        org.springframework.test.util.ReflectionTestUtils.setField(history, "scannedAt", LocalDateTime.now());

        when(scanHistoryRepository.findByIdAndUserId(42L, 1L)).thenReturn(Optional.of(history));

        ScanResultDto result = scanService.getScanById(42L, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Late Blight", result.diseaseLabel());
        assertEquals(92.0, result.confidenceScore());
    }

    @Test
    void shouldThrowWhenAccessingOtherUsersScan() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                scanService.getScanById(99L, "farmer@example.com"));

        assertTrue(ex.getMessage().contains("Scan not found or access denied"));
    }

    @Test
    void shouldSuccessfullyScanNutrientDeficiencyLeafAndAttachRemedies() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(mockFile)).thenReturn("nutrient-uuid.jpg");

        // External AI API returns "Nutrient deficiency"
        ApiPrediction prediction = new ApiPrediction(
                "Nutrient deficiency", 87.5, false, "{\"mock\": true}"
        );
        when(plantApiClient.classify(mockFile)).thenReturn(prediction);

        DiseaseInfo deficiency = new DiseaseInfo();
        deficiency.setId(11L);
        deficiency.setDiseaseName("Nutrient Deficiency");
        deficiency.setScientificName("Abiotic / Nutritional Disorder");
        deficiency.setDescription("Nutrient deficiency in tomato crops.");
        deficiency.setSymptoms("Chlorosis of foliage.");
        deficiency.setCauses("Soil pH imbalance or nutrient depletion.");
        deficiency.setPreventionTips("Soil testing and balanced fertilization.");
        deficiency.setSeverityLevel(DiseaseInfo.SeverityLevel.LOW);

        DiseaseRemedy organicRemedy = new DiseaseRemedy();
        organicRemedy.setId(201L);
        organicRemedy.setRemedyType(DiseaseRemedy.RemedyType.ORGANIC);
        organicRemedy.setRemedyName("Compost and Organic Soil Conditioning");
        organicRemedy.setApplication("Apply well-rotted compost based on soil test.");
        deficiency.getRemedies().add(organicRemedy);

        DiseaseRemedy chemicalRemedy = new DiseaseRemedy();
        chemicalRemedy.setId(202L);
        chemicalRemedy.setRemedyType(DiseaseRemedy.RemedyType.CHEMICAL);
        chemicalRemedy.setRemedyName("Targeted Soil Amendment Based on Soil Test");
        chemicalRemedy.setApplication("Conduct soil testing to identify deficient nutrient.");
        deficiency.getRemedies().add(chemicalRemedy);

        // Case-insensitive lookup resolves "Nutrient deficiency" to "Nutrient Deficiency"
        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase("Nutrient deficiency"))
                .thenReturn(Optional.of(deficiency));

        ScanResultDto result = scanService.scan(mockFile, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Nutrient deficiency", result.diseaseLabel());
        assertFalse(result.isHealthy());
        assertEquals(87.5, result.confidenceScore());
        assertNotNull(result.diseaseDetail());
        assertEquals("Nutrient Deficiency", result.diseaseDetail().diseaseName());
        assertEquals(2, result.diseaseDetail().remedies().size());

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository, times(1)).save(captor.capture());
        assertEquals(deficiency, captor.getValue().getDiseaseInfo());
    }

    @Test
    void shouldLookupExistingDiseaseWithTomatoPrefixNormalization() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(mockFile)).thenReturn("late-blight-uuid.jpg");

        // AI API returns "Late Blight" without "Tomato " prefix
        ApiPrediction prediction = new ApiPrediction(
                "Late Blight", 93.0, false, "{\"mock\": true}"
        );
        when(plantApiClient.classify(mockFile)).thenReturn(prediction);

        DiseaseInfo lateBlight = new DiseaseInfo();
        lateBlight.setId(2L);
        lateBlight.setDiseaseName("Tomato Late Blight");
        lateBlight.setSeverityLevel(DiseaseInfo.SeverityLevel.HIGH);

        // Direct lookup for "Late Blight" fails, but normalized "Tomato Late Blight" lookup succeeds
        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase("Late Blight")).thenReturn(Optional.empty());
        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase("Tomato Late Blight")).thenReturn(Optional.of(lateBlight));

        ScanResultDto result = scanService.scan(mockFile, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Late Blight", result.diseaseLabel());
        assertNotNull(result.diseaseDetail());
        assertEquals("Tomato Late Blight", result.diseaseDetail().diseaseName());

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository, times(1)).save(captor.capture());
        assertEquals(lateBlight, captor.getValue().getDiseaseInfo());
    }

    @Test
    void shouldHandleUnmatchedDiseaseGracefullyWithoutRemedies() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(fileStorageService.store(mockFile)).thenReturn("unknown-uuid.jpg");

        ApiPrediction prediction = new ApiPrediction(
                "Unrecognized Pathogen X", 55.0, false, "{\"mock\": true}"
        );
        when(plantApiClient.classify(mockFile)).thenReturn(prediction);

        // Repository finds no match for raw or normalized names
        when(diseaseInfoRepository.findByDiseaseNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        ScanResultDto result = scanService.scan(mockFile, "farmer@example.com");

        assertNotNull(result);
        assertEquals("Unrecognized Pathogen X", result.diseaseLabel());
        assertFalse(result.isHealthy());
        assertNull(result.diseaseDetail());

        ArgumentCaptor<ScanHistory> captor = ArgumentCaptor.forClass(ScanHistory.class);
        verify(scanHistoryRepository, times(1)).save(captor.capture());
        assertNull(captor.getValue().getDiseaseInfo());
        assertEquals("Unrecognized Pathogen X", captor.getValue().getDiseaseLabel());
    }
}
