package com.agrodoc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT utility: creates, parses, and validates JWT tokens.
 * Uses JJWT 0.12.x API (Keys.hmacShaKeyFor, Jwts.parser()).
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    @Value("${jwt.remember-me-expiration-ms:604800000}")
    private long rememberMeExpirationMs;

    private volatile SecretKey cachedKey;

    @PostConstruct
    public void init() {
        this.cachedKey = buildSignKey();
    }

    /**
     * Generate a JWT token for the given email address.
     *
     * @param email      The subject (user email)
     * @param rememberMe If true, uses the longer expiry duration
     * @return Signed JWT string
     */
    public String generateToken(String email, boolean rememberMe) {
        long expiry = rememberMe ? rememberMeExpirationMs : expirationMs;
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Extract the email (subject) from a JWT token.
     *
     * @param token The JWT string
     * @return Email stored in the subject claim
     */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validate the token signature and expiry.
     *
     * @param token The JWT string
     * @return true if valid, false otherwise
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignKey() {
        if (this.cachedKey == null) {
            synchronized (this) {
                if (this.cachedKey == null) {
                    this.cachedKey = buildSignKey();
                }
            }
        }
        return this.cachedKey;
    }

    private SecretKey buildSignKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is not configured. Please set jwt.secret or JWT_SECRET.");
        }

        byte[] keyBytes = null;

        // 1. Try standard Base64 decoding first (recommended format for production)
        try {
            byte[] decoded = Decoders.BASE64.decode(secret.trim());
            if (decoded.length >= 32) {
                keyBytes = decoded;
            }
        } catch (DecodingException | IllegalArgumentException e) {
            log.debug("Secret is not standard Base64: {}", e.getMessage());
        }

        // 2. Try Base64URL decoding if standard Base64 did not yield a valid key
        if (keyBytes == null) {
            try {
                byte[] decoded = Decoders.BASE64URL.decode(secret.trim());
                if (decoded.length >= 32) {
                    keyBytes = decoded;
                }
            } catch (DecodingException | IllegalArgumentException e) {
                log.debug("Secret is not Base64URL: {}", e.getMessage());
            }
        }

        // 3. Fall back to raw UTF-8 bytes if the secret is a plain text passphrase
        if (keyBytes == null) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Package-private helpers for testing
    void setSecret(String secret) {
        this.secret = secret;
        this.cachedKey = buildSignKey();
    }

    void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    void setRememberMeExpirationMs(long rememberMeExpirationMs) {
        this.rememberMeExpirationMs = rememberMeExpirationMs;
    }

    SecretKey getSigningKey() {
        return getSignKey();
    }
}
