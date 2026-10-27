package com.agrodoc.controller;

import com.agrodoc.dto.ScanResultDto;
import com.agrodoc.service.FileStorageService;
import com.agrodoc.service.ScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

/**
 * Controller for disease detection scans and image retrieval.
 * - POST /api/scan/upload : Upload tomato leaf image, run AI diagnosis, save history
 * - GET /api/scan/{id} & GET /api/scan/history/{id} : Retrieve scan diagnosis result
 * - GET /api/scan/images/{filename} : Serve uploaded image safely
 */
@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private static final Logger log = LoggerFactory.getLogger(ScanController.class);

    private final ScanService scanService;
    private final FileStorageService fileStorageService;

    public ScanController(ScanService scanService, FileStorageService fileStorageService) {
        this.scanService = scanService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * POST /api/scan/upload
     * Accepts a multipart tomato leaf image, classifies it using the external AI service,
     * looks up remedies in the database, saves to user history, and returns full scan results.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ScanResultDto> uploadScan(
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "file", required = false) MultipartFile fileBackup,
            Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new BadCredentialsException("Authentication required. Please log in to perform leaf scans.");
        }

        MultipartFile targetFile = (image != null && !image.isEmpty()) ? image : fileBackup;
        if (targetFile == null || targetFile.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file to upload.");
        }

        log.info("Received scan upload request from user '{}' (file: '{}', size: {} bytes)",
                principal.getName(), targetFile.getOriginalFilename(), targetFile.getSize());

        ScanResultDto result = scanService.scan(targetFile, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * GET /api/scan/history/{id} and GET /api/scan/{id}
     * Retrieve a specific scan result with full disease detail and remedies.
     * Enforces ownership: only the user who created the scan can access it.
     */
    @GetMapping({"/history/{id}", "/{id}"})
    public ResponseEntity<ScanResultDto> getScanById(
            @PathVariable("id") Long id,
            Principal principal) {

        if (principal == null || principal.getName() == null) {
            throw new BadCredentialsException("Authentication required.");
        }

        ScanResultDto result = scanService.getScanById(id, principal.getName());
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/scan/images/{filename:.+}
     * Safe retrieval of previously uploaded scan images for UI rendering.
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getScanImage(@PathVariable("filename") String filename) {
        Resource resource = fileStorageService.loadAsResource(filename);
        String contentType = fileStorageService.getContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
