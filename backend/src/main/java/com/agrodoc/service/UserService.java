package com.agrodoc.service;

import com.agrodoc.dto.ChangePasswordRequest;
import com.agrodoc.dto.UpdateProfileRequest;
import com.agrodoc.dto.UserProfileDto;
import com.agrodoc.dto.UserStatsDto;
import com.agrodoc.entity.ScanHistory;
import com.agrodoc.entity.User;
import com.agrodoc.repository.ScanHistoryRepository;
import com.agrodoc.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing user profile updates, password changes, and dashboard statistics.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ScanHistoryRepository scanHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ScanHistoryRepository scanHistoryRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.scanHistoryRepository = scanHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves the profile information for the specified user email.
     */
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String email) {
        User user = getUserByEmail(email);
        return toProfileDto(user);
    }

    /**
     * Updates the user's display name.
     */
    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileRequest request) {
        User user = getUserByEmail(email);

        String newFullName = request.fullName() != null ? request.fullName().trim() : "";
        if (newFullName.length() < 2 || newFullName.length() > 100) {
            throw new IllegalArgumentException("Full name must be between 2 and 100 characters.");
        }

        user.setFullName(newFullName);
        User updated = userRepository.save(user);
        log.info("Updated display name for user '{}' to '{}'", email, newFullName);
        return toProfileDto(updated);
    }

    /**
     * Updates the user's password after verifying the existing password with BCrypt.
     */
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);

        String oldPassword = request.getEffectiveOldPassword();
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match.");
        }

        String newPassword = request.newPassword() != null ? request.newPassword().trim() : "";
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters.");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password successfully updated for user '{}'", email);
    }

    /**
     * Aggregates scan metrics and recent scans for the dashboard.
     */
    @Transactional(readOnly = true)
    public UserStatsDto getUserStats(String email) {
        User user = getUserByEmail(email);
        Long userId = user.getId();

        long totalScans = scanHistoryRepository.countByUserId(userId);
        List<ScanHistory> top3Scans = scanHistoryRepository.findTop3ByUserIdOrderByScannedAtDesc(userId);

        LocalDateTime lastScanDate = top3Scans.isEmpty() ? null : top3Scans.get(0).getScannedAt();

        List<Object[]> diseaseCounts = scanHistoryRepository.findMostCommonDiseaseByUserId(userId);
        String mostCommonDisease;
        if (!diseaseCounts.isEmpty() && diseaseCounts.get(0).length > 0 && diseaseCounts.get(0)[0] != null) {
            mostCommonDisease = (String) diseaseCounts.get(0)[0];
        } else if (totalScans > 0) {
            mostCommonDisease = "Tomato Healthy";
        } else {
            mostCommonDisease = "None";
        }

        List<UserStatsDto.RecentScanDto> recentScans = top3Scans.stream()
                .map(scan -> new UserStatsDto.RecentScanDto(
                        scan.getId(),
                        scan.getDiseaseLabel(),
                        scan.getConfidenceScore(),
                        scan.isHealthy(),
                        scan.getImageFilename(),
                        "/api/scan/images/" + scan.getImageFilename(),
                        scan.getScannedAt()
                ))
                .toList();

        return new UserStatsDto(
                totalScans,
                mostCommonDisease,
                mostCommonDisease,
                lastScanDate,
                recentScans
        );
    }

    private User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be null or blank.");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    }

    private UserProfileDto toProfileDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

