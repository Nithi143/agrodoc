package com.agrodoc.controller;

import com.agrodoc.dto.ScanResultDto;
import com.agrodoc.service.FileStorageService;
import com.agrodoc.service.PlantApiClient;
import com.agrodoc.service.ScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScanService scanService;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldUploadImageAndReturnScanResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        ScanResultDto dto = new ScanResultDto(
                1L, "Early Blight", false, 95.0, "stored.jpg", "2026-10-27T10:00:00", null
        );

        when(scanService.scan(any(), eq("farmer@example.com"))).thenReturn(dto);

        mockMvc.perform(multipart("/api/scan/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scanId").value(1))
                .andExpect(jsonPath("$.diseaseLabel").value("Early Blight"))
                .andExpect(jsonPath("$.confidenceScore").value(95.0))
                .andExpect(jsonPath("$.isHealthy").value(false));
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldReturnHttp503WhenPlantApiKeyIsMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(scanService.scan(any(), eq("farmer@example.com")))
                .thenThrow(new PlantApiClient.ApiNotConfiguredException(
                        "The Plant Disease AI API key is not yet configured. Please add your plant.api.key to application-dev.properties and restart the server."
                ));

        mockMvc.perform(multipart("/api/scan/upload").file(file))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not yet configured")));
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldReturnScanById() throws Exception {
        ScanResultDto dto = new ScanResultDto(
                42L, "Late Blight", false, 88.5, "late-blight.jpg", "2026-10-27T10:00:00", null
        );

        when(scanService.getScanById(42L, "farmer@example.com")).thenReturn(dto);

        mockMvc.perform(get("/api/scan/history/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scanId").value(42))
                .andExpect(jsonPath("$.diseaseLabel").value("Late Blight"));
    }

    @Test
    void shouldServeUploadedImageWithoutRequiringAuthentication() throws Exception {
        ByteArrayResource imageResource = new ByteArrayResource(new byte[]{1, 2, 3, 4});
        when(fileStorageService.loadAsResource("test-sample.jpg")).thenReturn(imageResource);
        when(fileStorageService.getContentType("test-sample.jpg")).thenReturn("image/jpeg");

        mockMvc.perform(get("/api/scan/images/test-sample.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }
}
