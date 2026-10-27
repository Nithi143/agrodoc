package com.agrodoc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    // Standard valid JPEG header bytes: FF D8 FF E0
    private static final byte[] VALID_JPEG_BYTES = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};

    // Standard valid PNG header bytes: 89 50 4E 47 0D 0A 1A 0A
    private static final byte[] VALID_PNG_BYTES = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};

    // Standard valid WebP header bytes: RIFF....WEBP
    private static final byte[] VALID_WEBP_BYTES = new byte[]{'R', 'I', 'F', 'F', 0x24, 0x00, 0x00, 0x00, 'W', 'E', 'B', 'P'};

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(fileStorageService, "maxSizeBytes", 5242880L);
        fileStorageService.init();
    }

    @Test
    void shouldStoreValidJpegFileAndReturnUuidFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.jpg", "image/jpeg", VALID_JPEG_BYTES
        );

        String savedFilename = fileStorageService.store(file);

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".jpg"));
        assertNotEquals("leaf.jpg", savedFilename); // Must be a generated UUID filename
        Resource resource = fileStorageService.loadAsResource(savedFilename);
        assertTrue(resource.exists());
    }

    @Test
    void shouldStoreValidPngFile() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.png", "image/png", VALID_PNG_BYTES
        );

        String savedFilename = fileStorageService.store(file);

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".png"));
    }

    @Test
    void shouldStoreValidWebpFile() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "leaf.webp", "image/webp", VALID_WEBP_BYTES
        );

        String savedFilename = fileStorageService.store(file);

        assertNotNull(savedFilename);
        assertTrue(savedFilename.endsWith(".webp"));
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "empty.jpg", "image/jpeg", new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(emptyFile));
        assertTrue(ex.getMessage().contains("Please select an image file to upload"));
    }

    @Test
    void shouldRejectFileSizeExceedingLimit() {
        byte[] largeBytes = new byte[5242881]; // 5MB + 1 byte
        System.arraycopy(VALID_JPEG_BYTES, 0, largeBytes, 0, VALID_JPEG_BYTES.length);

        MockMultipartFile largeFile = new MockMultipartFile(
                "image", "large.jpg", "image/jpeg", largeBytes
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(largeFile));
        assertTrue(ex.getMessage().contains("exceeds the maximum allowed limit"));
    }

    @Test
    void shouldRejectDisallowedMimeType() {
        MockMultipartFile textFile = new MockMultipartFile(
                "image", "script.sh", "text/plain", new byte[]{0x23, 0x21, 0x2F, 0x62, 0x69, 0x6E}
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(textFile));
        assertTrue(ex.getMessage().contains("Invalid file type"));
    }

    @Test
    void shouldRejectSpoofedMagicBytes() {
        // Content-Type says image/jpeg, but content is plaintext ASCII
        MockMultipartFile spoofedFile = new MockMultipartFile(
                "image", "spoofed.jpg", "image/jpeg", "<?php echo 'hello'; ?>".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.store(spoofedFile));
        assertTrue(ex.getMessage().contains("does not match a valid"));
    }

    @Test
    void shouldRejectPathTraversalInFilenameWhenLoading() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.loadAsResource("../secret.txt"));
        assertTrue(ex.getMessage().contains("Invalid filename format"));
    }

    @Test
    void shouldThrowWhenLoadingNonExistentFile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> fileStorageService.loadAsResource("non-existent-uuid.jpg"));
        assertTrue(ex.getMessage().contains("does not exist or is not readable"));
    }

    @Test
    void shouldCorrectlyDetectContentType() {
        assertEquals("image/jpeg", fileStorageService.getContentType("photo.jpg"));
        assertEquals("image/jpeg", fileStorageService.getContentType("photo.jpeg"));
        assertEquals("image/png", fileStorageService.getContentType("photo.png"));
        assertEquals("image/webp", fileStorageService.getContentType("photo.webp"));
        assertEquals("application/octet-stream", fileStorageService.getContentType("photo.unknown"));
    }
}
