package com.agrodoc.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

    @Value("${jwt.remember-me-expiration-ms}")
    private long rememberMeExpirationMs;

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
        byte[] keyBytes;
        try {
            // Try Base64-decoded first (most secure key format)
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            // Fall back to raw UTF-8 bytes if secret is a plain string
            keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
