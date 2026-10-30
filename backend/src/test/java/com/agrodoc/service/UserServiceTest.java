package com.agrodoc.service;

import com.agrodoc.dto.ChangePasswordRequest;
import com.agrodoc.dto.UpdateProfileRequest;
import com.agrodoc.dto.UserProfileDto;
import com.agrodoc.dto.UserStatsDto;
import com.agrodoc.entity.ScanHistory;
import com.agrodoc.entity.User;
import com.agrodoc.repository.ScanHistoryRepository;
import com.agrodoc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Farmer John");
        testUser.setEmail("farmer@example.com");
        testUser.setPasswordHash("$2a$12$hashedPassword");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(10));
        testUser.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void shouldReturnUserProfileSuccessfully() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));

        UserProfileDto profile = userService.getUserProfile("farmer@example.com");

        assertThat(profile).isNotNull();
        assertThat(profile.id()).isEqualTo(1L);
        assertThat(profile.fullName()).isEqualTo("Farmer John");
        assertThat(profile.email()).isEqualTo("farmer@example.com");
        assertThat(profile.createdAt()).isEqualTo(testUser.getCreatedAt());
    }

    @Test
    void shouldThrowWhenUserNotFoundInProfile() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("No user found with email");
    }

    @Test
    void shouldUpdateProfileSuccessfully() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest("Johnathan Doe");
        UserProfileDto updated = userService.updateProfile("farmer@example.com", request);

        assertThat(updated.fullName()).isEqualTo("Johnathan Doe");
        verify(userRepository).save(testUser);
        assertThat(testUser.getFullName()).isEqualTo("Johnathan Doe");
    }

    @Test
    void shouldThrowWhenUpdateProfileNameIsTooShort() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));

        UpdateProfileRequest request = new UpdateProfileRequest("A");

        assertThatThrownBy(() -> userService.updateProfile("farmer@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 2 and 100 characters");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$hashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewSecret456", "$2a$12$hashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewSecret456")).thenReturn("$2a$12$newHashedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123", null, "NewSecret456");
        userService.changePassword("farmer@example.com", request);

        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$12$newHashedPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldChangePasswordWithOldPasswordAlias() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$hashedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewSecret456", "$2a$12$hashedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewSecret456")).thenReturn("$2a$12$newHashedPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(null, "OldPassword123", "NewSecret456");
        userService.changePassword("farmer@example.com", request);

        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$12$newHashedPassword");
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldThrowWhenCurrentPasswordIsMissing() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));

        ChangePasswordRequest request = new ChangePasswordRequest(null, "", "NewSecret456");

        assertThatThrownBy(() -> userService.changePassword("farmer@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is required");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCurrentPasswordDoesNotMatch() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "$2a$12$hashedPassword")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPassword", null, "NewSecret456");

        assertThatThrownBy(() -> userService.changePassword("farmer@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password does not match");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNewPasswordIsSameAsCurrentPassword() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("SamePassword123", "$2a$12$hashedPassword")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("SamePassword123", null, "SamePassword123");

        assertThatThrownBy(() -> userService.changePassword("farmer@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be the same");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenNewPasswordIsTooShort() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("OldPassword123", "$2a$12$hashedPassword")).thenReturn(true);

        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123", null, "short");

        assertThatThrownBy(() -> userService.changePassword("farmer@example.com", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 8 characters");

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnUserStatsWithScansAndDisease() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countByUserId(1L)).thenReturn(5L);

        ScanHistory scan1 = new ScanHistory();
        scan1.setId(101L);
        scan1.setDiseaseLabel("Early Blight");
        scan1.setConfidenceScore(new BigDecimal("94.50"));
        scan1.setHealthy(false);
        scan1.setImageFilename("leaf-1.jpg");
        scan1.setScannedAt(LocalDateTime.of(2026, 10, 28, 14, 30));

        ScanHistory scan2 = new ScanHistory();
        scan2.setId(102L);
        scan2.setDiseaseLabel("Early Blight");
        scan2.setConfidenceScore(new BigDecimal("88.00"));
        scan2.setHealthy(false);
        scan2.setImageFilename("leaf-2.jpg");
        scan2.setScannedAt(LocalDateTime.of(2026, 10, 27, 9, 15));

        when(scanHistoryRepository.findTop3ByUserIdOrderByScannedAtDesc(1L)).thenReturn(List.of(scan1, scan2));

        List<Object[]> diseaseCounts = new ArrayList<>();
        diseaseCounts.add(new Object[]{"Early Blight", 3L});
        when(scanHistoryRepository.findMostCommonDiseaseByUserId(1L)).thenReturn(diseaseCounts);

        UserStatsDto stats = userService.getUserStats("farmer@example.com");

        assertThat(stats.totalScans()).isEqualTo(5L);
        assertThat(stats.mostFrequentDisease()).isEqualTo("Early Blight");
        assertThat(stats.mostCommonDisease()).isEqualTo("Early Blight");
        assertThat(stats.lastScanDate()).isEqualTo(LocalDateTime.of(2026, 10, 28, 14, 30));
        assertThat(stats.recentScans()).hasSize(2);
        assertThat(stats.recentScans().get(0).diseaseLabel()).isEqualTo("Early Blight");
        assertThat(stats.recentScans().get(0).imageUrl()).isEqualTo("/api/scan/images/leaf-1.jpg");
    }

    @Test
    void shouldReturnUserStatsWhenNoScans() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countByUserId(1L)).thenReturn(0L);
        when(scanHistoryRepository.findTop3ByUserIdOrderByScannedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(scanHistoryRepository.findMostCommonDiseaseByUserId(1L)).thenReturn(Collections.emptyList());

        UserStatsDto stats = userService.getUserStats("farmer@example.com");

        assertThat(stats.totalScans()).isEqualTo(0L);
        assertThat(stats.mostFrequentDisease()).isEqualTo("None");
        assertThat(stats.mostCommonDisease()).isEqualTo("None");
        assertThat(stats.lastScanDate()).isNull();
        assertThat(stats.recentScans()).isEmpty();
    }

    @Test
    void shouldReturnUserStatsWhenOnlyHealthyScans() {
        when(userRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(testUser));
        when(scanHistoryRepository.countByUserId(1L)).thenReturn(2L);

        ScanHistory healthyScan = new ScanHistory();
        healthyScan.setId(201L);
        healthyScan.setDiseaseLabel("Tomato Healthy");
        healthyScan.setConfidenceScore(new BigDecimal("98.00"));
        healthyScan.setHealthy(true);
        healthyScan.setImageFilename("healthy-leaf.jpg");
        healthyScan.setScannedAt(LocalDateTime.of(2026, 10, 29, 10, 0));

        when(scanHistoryRepository.findTop3ByUserIdOrderByScannedAtDesc(1L)).thenReturn(List.of(healthyScan));
        when(scanHistoryRepository.findMostCommonDiseaseByUserId(1L)).thenReturn(Collections.emptyList());

        UserStatsDto stats = userService.getUserStats("farmer@example.com");

        assertThat(stats.totalScans()).isEqualTo(2L);
        assertThat(stats.mostFrequentDisease()).isEqualTo("Tomato Healthy");
        assertThat(stats.mostCommonDisease()).isEqualTo("Tomato Healthy");
        assertThat(stats.lastScanDate()).isEqualTo(LocalDateTime.of(2026, 10, 29, 10, 0));
        assertThat(stats.recentScans()).hasSize(1);
    }
}

