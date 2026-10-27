package com.agrodoc.dto;

import java.util.List;

/**
 * Error response DTO returned by GlobalExceptionHandler.
 * All backend errors use this consistent format.
 *
 * { "status": 400, "message": "...", "details": { "field": "error" } }
 */
public record ErrorResponse(
    int status,
    String message,
    Object details
) {
    /** Convenience constructor for simple errors without field details. */
    public ErrorResponse(int status, String message) {
        this(status, message, null);
    }
}
