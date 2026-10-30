package com.agrodoc.dto;

import java.time.LocalDateTime;

/**
 * Response DTO containing the authenticated user's profile information.
 * Password hash and sensitive security credentials are never exposed.
 */
public record UserProfileDto(
    Long id,
    String fullName,
    String email,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

