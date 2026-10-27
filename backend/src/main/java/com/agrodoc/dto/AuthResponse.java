package com.agrodoc.dto;

/**
 * Response DTO returned after a successful login.
 * Contains the JWT token and basic user profile.
 */
public record AuthResponse(
    String token,
    UserDto user
) {
    public record UserDto(
        Long id,
        String fullName,
        String email
    ) {}
}
