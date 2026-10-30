package com.agrodoc.controller;

import com.agrodoc.dto.ChangePasswordRequest;
import com.agrodoc.dto.UpdateProfileRequest;
import com.agrodoc.dto.UserProfileDto;
import com.agrodoc.dto.UserStatsDto;
import com.agrodoc.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller for authenticated user management: profile, password changes, and stats.
 * Endpoints are secured under /api/users and operate in the context of the calling user.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users/me
     * Returns the currently authenticated user's profile.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Principal principal) {
        String email = getAuthenticatedEmail(principal);
        UserProfileDto profile = userService.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * PATCH /api/users/me
     * Updates the user's display name.
     */
    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal principal) {
        String email = getAuthenticatedEmail(principal);
        UserProfileDto updatedProfile = userService.updateProfile(email, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * POST /api/users/me/change-password
     * Verifies current password and updates to a newly encrypted password.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        String email = getAuthenticatedEmail(principal);
        userService.changePassword(email, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    /**
     * GET /api/users/me/stats
     * Aggregates scan counts, most common diagnosis, last scan date, and top 3 recent scans.
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsDto> getUserStats(Principal principal) {
        String email = getAuthenticatedEmail(principal);
        UserStatsDto stats = userService.getUserStats(email);
        return ResponseEntity.ok(stats);
    }

    private String getAuthenticatedEmail(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new BadCredentialsException("Authentication required. Please log in.");
        }
        return principal.getName();
    }
}

