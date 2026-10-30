package com.agrodoc.controller;

import com.agrodoc.dto.ChangePasswordRequest;
import com.agrodoc.dto.UpdateProfileRequest;
import com.agrodoc.dto.UserProfileDto;
import com.agrodoc.dto.UserStatsDto;
import com.agrodoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldGetCurrentUserProfileWhenAuthenticated() throws Exception {
        UserProfileDto profile = new UserProfileDto(
                1L,
                "Farmer John",
                "farmer@example.com",
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 20, 12, 0)
        );

        when(userService.getUserProfile("farmer@example.com")).thenReturn(profile);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Farmer John"))
                .andExpect(jsonPath("$.email").value("farmer@example.com"));

        verify(userService, times(1)).getUserProfile("farmer@example.com");
    }

    @Test
    void shouldRejectUnauthenticatedAccessForCurrentUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldUpdateProfileWhenValid() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Farmer");
        UserProfileDto updatedProfile = new UserProfileDto(
                1L,
                "Jane Farmer",
                "farmer@example.com",
                LocalDateTime.of(2026, 10, 15, 10, 0),
                LocalDateTime.of(2026, 10, 30, 12, 0)
        );

        when(userService.updateProfile(eq("farmer@example.com"), any(UpdateProfileRequest.class)))
                .thenReturn(updatedProfile);

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane Farmer"))
                .andExpect(jsonPath("$.email").value("farmer@example.com"));

        verify(userService, times(1)).updateProfile(eq("farmer@example.com"), any(UpdateProfileRequest.class));
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldReturnBadRequestWhenUpdateProfileInvalid() throws Exception {
        UpdateProfileRequest invalidRequest = new UpdateProfileRequest("A");

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.fullName").exists());

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldChangePasswordSuccessfully() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123", null, "NewPassword456"
        );

        doNothing().when(userService).changePassword(eq("farmer@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully."));

        verify(userService, times(1)).changePassword(eq("farmer@example.com"), any(ChangePasswordRequest.class));
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldReturnBadRequestWhenCurrentPasswordDoesNotMatch() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPassword", null, "NewPassword456"
        );

        doThrow(new IllegalArgumentException("Current password does not match."))
                .when(userService).changePassword(eq("farmer@example.com"), any(ChangePasswordRequest.class));

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password does not match."));
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldReturnBadRequestWhenNewPasswordTooShort() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "OldPassword123", null, "short"
        );

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.newPassword").exists());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @WithMockUser(username = "farmer@example.com")
    void shouldGetUserStatsSuccessfully() throws Exception {
        UserStatsDto.RecentScanDto recentScan = new UserStatsDto.RecentScanDto(
                10L,
                "Nutrient Deficiency",
                new BigDecimal("91.50"),
                false,
                "leaf-def.jpg",
                "/api/scan/images/leaf-def.jpg",
                LocalDateTime.of(2026, 10, 29, 15, 0)
        );

        UserStatsDto stats = new UserStatsDto(
                4L,
                "Nutrient Deficiency",
                "Nutrient Deficiency",
                LocalDateTime.of(2026, 10, 29, 15, 0),
                List.of(recentScan)
        );

        when(userService.getUserStats("farmer@example.com")).thenReturn(stats);

        mockMvc.perform(get("/api/users/me/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalScans").value(4))
                .andExpect(jsonPath("$.mostFrequentDisease").value("Nutrient Deficiency"))
                .andExpect(jsonPath("$.mostCommonDisease").value("Nutrient Deficiency"))
                .andExpect(jsonPath("$.recentScans[0].diseaseLabel").value("Nutrient Deficiency"))
                .andExpect(jsonPath("$.recentScans[0].confidenceScore").value(91.50));

        verify(userService, times(1)).getUserStats("farmer@example.com");
    }
}

