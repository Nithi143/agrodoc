package com.agrodoc.exception;

import com.agrodoc.dto.ErrorResponse;
import com.agrodoc.service.PlantApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnHttp503WhenPlantApiKeyIsUnconfigured() {
        PlantApiClient.ApiNotConfiguredException ex = new PlantApiClient.ApiNotConfiguredException(
                "The Plant Disease AI API key is not yet configured."
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleApiNotConfigured(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals("The Plant Disease AI API key is not yet configured.", response.getBody().message());
    }

    @Test
    void shouldReturnHttp502WhenPlantApiCallFails() {
        PlantApiClient.ApiCallException ex = new PlantApiClient.ApiCallException(
                "Plant.id API is unreachable", new RuntimeException("connection timeout")
        );

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleApiCallFailed(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
    }

    @Test
    void shouldReturnHttp401OnBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Invalid email or password.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().status());
    }

    @Test
    void shouldReturnHttp403OnAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
    }

    @Test
    void shouldReturnHttp404WhenUsernameNotFound() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
    }

    @Test
    void shouldReturnHttp400OnIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid file format");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
    }

    @Test
    void shouldReturnHttp413OnMaxUploadSizeExceeded() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5242880);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxSizeExceeded(ex);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(413, response.getBody().status());
    }

    @Test
    void shouldReturnHttp400WithFieldDetailsOnValidationFailure() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("registerRequest", "email", "Must be a valid email");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) response.getBody().details();
        assertEquals("Must be a valid email", details.get("email"));
    }

    @Test
    void shouldReturnHttp500OnUnexpectedExceptionWithoutLeakingStackTrace() {
        Exception ex = new RuntimeException("DB connection dropped");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("An unexpected server error occurred. Please try again later.", response.getBody().message());
    }
}
