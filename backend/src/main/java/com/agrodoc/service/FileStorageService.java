package com.agrodoc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Service for securely storing and retrieving uploaded tomato leaf images.
 * Enforces strict security constraints:
 * - Allowed MIME types: image/jpeg, image/png, image/webp
 * - Allowed file extensions: .jpg, .jpeg, .png, .webp
 * - Magic byte inspection to verify actual file headers
 * - Maximum file size check (5 MB)
 * - Randomized UUID filenames to prevent file overwrite and malicious naming
 * - Path traversal protection
 * - Storage directory located outside web-accessible paths
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    @Value("${agrodoc.upload.dir:uploads}")
    private String uploadDir;

    @Value("${agrodoc.upload.max-size-bytes:5242880}")
    private long maxSizeBytes;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Initialized file storage location at: {}", this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload storage folder: " + this.rootLocation, e);
        }
    }

    /**
     * Validate and securely store an uploaded image file.
     *
     * @param file The uploaded MultipartFile
     * @return Generated safe UUID filename (e.g. 123e4567-e89b-12d3-a456-426614174000.jpg)
     */
    public String store(MultipartFile file) {
        validateImage(file);

        String extension = extractAndValidateExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + extension;

        try {
            Path destination = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!destination.startsWith(this.rootLocation)) {
                throw new SecurityException("Path traversal attempt detected. Cannot store file outside upload directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Securely stored upload as '{}' (size: {} bytes)", filename, file.getSize());
            return filename;
        } catch (IOException e) {
            log.error("Failed to store uploaded file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to store uploaded file. Please try again.", e);
        }
    }

    /**
     * Load an image file as a Spring Resource for viewing.
     *
     * @param filename The sanitized image filename
     * @return UrlResource representing the image file
     */
    public Resource loadAsResource(String filename) {
        if (!StringUtils.hasText(filename) || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename format.");
        }

        try {
            Path file = this.rootLocation.resolve(filename).normalize().toAbsolutePath();
            if (!file.startsWith(this.rootLocation) || !Files.exists(file) || !Files.isReadable(file)) {
                throw new IllegalArgumentException("Requested image does not exist or is not readable: " + filename);
            }

            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Could not read image file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed image file path: " + filename, e);
        }
    }

    /**
     * Determine the HTTP MIME Content-Type based on file extension.
     */
    public String getContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    /**
     * Strict validation of the uploaded file:
     * 1. Null / empty checks
     * 2. Size limit (5 MB)
     * 3. MIME type inspection
     * 4. Magic byte signature verification
     */
    public void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file to upload.");
        }

        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("File size (%d KB) exceeds the maximum allowed limit of %d MB.",
                            file.getSize() / 1024, maxSizeBytes / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, and WebP images are supported.");
        }

        // Magic bytes validation
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4 || !matchesMagicBytes(header, read)) {
                throw new IllegalArgumentException("File content does not match a valid JPEG, PNG, or WebP image.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read image header bytes: " + e.getMessage(), e);
        }
    }

    private boolean matchesMagicBytes(byte[] header, int length) {
        // JPEG: FF D8 FF
        if (length >= 3 && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if (length >= 4 && (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
            return true;
        }
        // WebP: RIFF (bytes 0-3) ... WEBP (bytes 8-11)
        if (length >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return true;
        }
        return false;
    }

    private String extractAndValidateExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename) || !originalFilename.contains(".")) {
            return ".jpg";
        }

        int dotIndex = originalFilename.lastIndexOf('.');
        String ext = originalFilename.substring(dotIndex).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file extension '" + ext + "'. Allowed: .jpg, .jpeg, .png, .webp");
        }

        return ext;
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}
