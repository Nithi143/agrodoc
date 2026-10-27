package com.agrodoc.exception;

import com.agrodoc.dto.ErrorResponse;
import com.agrodoc.service.PlantApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for all AgroDoc REST API endpoints.
 * Maps application exceptions to consistent, structured {@link ErrorResponse} DTOs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * External AI API key missing or unconfigured — HTTP 503 Service Unavailable.
     * Prevents fake or mock diagnosis results from being returned when the real API is unavailable.
     */
    @ExceptionHandler(PlantApiClient.ApiNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleApiNotConfigured(PlantApiClient.ApiNotConfiguredException ex) {
        log.warn("AI service not configured: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), ex.getMessage()));
    }

    /**
     * External AI API call failed or timed out — HTTP 502 Bad Gateway.
     */
    @ExceptionHandler(PlantApiClient.ApiCallException.class)
    public ResponseEntity<ErrorResponse> handleApiCallFailed(PlantApiClient.ApiCallException ex) {
        log.error("AI service communication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(HttpStatus.BAD_GATEWAY.value(), ex.getMessage()));
    }

    /**
     * Request DTO validation errors (@Valid on request bodies) — HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.debug("Validation failed on request: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed. Please correct the highlighted fields.", fieldErrors));
    }

    /**
     * Bad credentials (invalid email or password on login) — HTTP 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
    }

    /**
     * Unauthorized resource access / ownership check failed — HTTP 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Access denied: you do not have permission to access this resource."));
    }

    /**
     * User or resource not found — HTTP 404 Not Found.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UsernameNotFoundException ex) {
        log.info("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    /**
     * Business logic violations / invalid input arguments — HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.info("Invalid request argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    /**
     * File upload exceeds maximum allowed size (5MB) — HTTP 413 Payload Too Large.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("File upload size exceeded limit: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), "The uploaded image exceeds the maximum permitted size of 5 MB."));
    }

    /**
     * Missing multipart file or request parameters — HTTP 400 Bad Request.
     */
    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> handleMultipartErrors(Exception ex) {
        log.warn("Multipart request error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Please select an image file to upload."));
    }

    /**
     * Fallback for any unexpected internal server error — HTTP 500 Internal Server Error.
     * Prevents stack traces from leaking to client responses.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled server exception caught in GlobalExceptionHandler", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected server error occurred. Please try again later."));
    }
}
