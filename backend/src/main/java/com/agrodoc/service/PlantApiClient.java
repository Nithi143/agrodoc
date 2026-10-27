package com.agrodoc.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP client for the external Plant.id v3 AI API.
 *
 * API documentation: https://plant.id/docs
 *
 * Normalizes the external API response into an {@link ApiPrediction} record.
 * Changing the external API only requires updating this class.
 *
 * If the API key is not configured ("NOT_YET_CONFIGURED"), throws
 * {@link ApiNotConfiguredException} so the controller can return HTTP 503.
 */
@Service
public class PlantApiClient {

    private static final Logger log = LoggerFactory.getLogger(PlantApiClient.class);
    private static final String UNCONFIGURED = "NOT_YET_CONFIGURED";

    @Value("${plant.api.key}")
    private String apiKey;

    @Value("${plant.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PlantApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send an image to the Plant.id API and return the top disease prediction.
     *
     * @param imageFile The uploaded tomato leaf image
     * @return Normalized prediction (diseaseName, confidence, isHealthy, rawJson)
     * @throws ApiNotConfiguredException if the API key is not yet set
     * @throws ApiCallException          for network errors or unexpected API failures
     */
    public ApiPrediction classify(MultipartFile imageFile) {
        if (apiKey == null || apiKey.trim().isEmpty() || UNCONFIGURED.equalsIgnoreCase(apiKey.trim()) || apiKey.contains("NOT_YET_CONFIGURED")) {
            throw new ApiNotConfiguredException(
                "The Plant Disease AI API key is not yet configured. " +
                "Please add your plant.api.key to application-dev.properties and restart the server."
            );
        }

        try {
            // Build multipart request to Plant.id health assessment endpoint
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Api-Key", apiKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("images", new org.springframework.core.io.ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            });
            // Plant.id v3 health assessment parameters
            body.add("health", "only");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            String endpoint = baseUrl + "/health_assessment";

            ResponseEntity<String> response = restTemplate.exchange(
                endpoint, HttpMethod.POST, requestEntity, String.class
            );

            return parseResponse(response.getBody());

        } catch (ApiNotConfiguredException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                log.error("Plant.id API key is invalid or unauthorized: {}", e.getMessage());
                throw new ApiCallException("The AI service API key is invalid. Please contact the administrator.", e);
            }
            log.error("Plant.id API client error {}: {}", e.getStatusCode(), e.getMessage());
            throw new ApiCallException("The AI service returned an error. Please try again.", e);
        } catch (ResourceAccessException e) {
            log.error("Plant.id API unreachable: {}", e.getMessage());
            throw new ApiCallException("The AI service is currently unreachable. Please check your connection and try again.", e);
        } catch (Exception e) {
            log.error("Unexpected error calling Plant.id API: {}", e.getMessage(), e);
            throw new ApiCallException("An unexpected error occurred while analyzing the image. Please try again.", e);
        }
    }

    /**
     * Parse the Plant.id v3 JSON response into a normalized ApiPrediction.
     * Expected structure:
     * {
     *   "result": {
     *     "is_healthy": { "binary": true/false, "probability": 0.95 },
     *     "disease": {
     *       "suggestions": [
     *         { "name": "Early blight", "probability": 0.91, ... }
     *       ]
     *     }
     *   }
     * }
     */
    private ApiPrediction parseResponse(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode result = root.path("result");

            // Check healthy status
            JsonNode healthNode = result.path("is_healthy");
            boolean isHealthy = healthNode.path("binary").asBoolean(false);
            double healthProbability = healthNode.path("probability").asDouble(0.0);

            if (isHealthy) {
                return new ApiPrediction(
                    "Healthy",
                    Math.round(healthProbability * 10000.0) / 100.0, // to 2 decimal places
                    true,
                    rawJson
                );
            }

            // Get top disease suggestion
            JsonNode suggestions = result.path("disease").path("suggestions");
            if (!suggestions.isArray() || suggestions.isEmpty()) {
                // API returned no disease — treat as unknown
                return new ApiPrediction("Unknown", 0.0, false, rawJson);
            }

            JsonNode topSuggestion = suggestions.get(0);
            String diseaseName = topSuggestion.path("name").asText("Unknown");
            double confidence = Math.round(topSuggestion.path("probability").asDouble(0.0) * 10000.0) / 100.0;

            return new ApiPrediction(diseaseName, confidence, false, rawJson);

        } catch (Exception e) {
            log.error("Failed to parse Plant.id API response: {}", e.getMessage(), e);
            throw new ApiCallException("Failed to interpret the AI analysis result. Please try again.", e);
        }
    }

    // =========================================================================
    // Inner types
    // =========================================================================

    /**
     * Normalized prediction result from the external AI API.
     *
     * @param diseaseName    Canonical disease name (should match disease_info.disease_name)
     * @param confidence     Confidence percentage (0.0 to 100.0)
     * @param isHealthy      True if classified as healthy
     * @param rawApiResponse Full JSON response string for storage in scan_history
     */
    public record ApiPrediction(
        String diseaseName,
        double confidence,
        boolean isHealthy,
        String rawApiResponse
    ) {}

    /**
     * Thrown when the API key is absent or unconfigured.
     * Results in HTTP 503 Service Unavailable.
     */
    public static class ApiNotConfiguredException extends RuntimeException {
        public ApiNotConfiguredException(String message) {
            super(message);
        }
    }

    /**
     * Thrown for network errors or API failures after a real API call attempt.
     * Results in HTTP 502 Bad Gateway.
     */
    public static class ApiCallException extends RuntimeException {
        public ApiCallException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
