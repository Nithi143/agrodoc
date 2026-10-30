package com.agrodoc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating user password.
 * POST /api/users/me/change-password
 *
 * Accepts either `currentPassword` or `oldPassword` for maximum compatibility.
 */
public record ChangePasswordRequest(

    String currentPassword,

    String oldPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    String newPassword

) {
    /**
     * Resolves the current password regardless of whether the client sent
     * `currentPassword` (used in frontend forms) or `oldPassword`.
     */
    public String getEffectiveOldPassword() {
        if (currentPassword != null && !currentPassword.isBlank()) {
            return currentPassword.trim();
        }
        if (oldPassword != null && !oldPassword.isBlank()) {
            return oldPassword.trim();
        }
        return null;
    }
}

