package com.agrodoc.security;

import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    // Valid Base64-encoded 48-byte (384-bit) key
    private static final String VALID_BASE64_SECRET =
            "7SpOFB+RiQzket6z1puQsVbTmxjzL9x6+XMFvM9MH56iiaav7dUVXTv73o1yJZpT";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", VALID_BASE64_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "rememberMeExpirationMs", 86400000L); // 24 hours
        jwtUtil.init();
    }

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        String email = "farmer@example.com";

        String token = jwtUtil.generateToken(email, false);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtUtil.isValid(token));
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void shouldGenerateAndValidateRememberMeToken() {
        String email = "grower@agrodoc.org";

        String token = jwtUtil.generateToken(email, true);

        assertNotNull(token);
        assertTrue(jwtUtil.isValid(token));
        assertEquals(email, jwtUtil.extractEmail(token));
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtUtil.generateToken("user@example.com", false);

        // Tamper with the token by modifying characters in the signature
        String tamperedToken = token.substring(0, token.length() - 5) + "ABCDE";

        assertFalse(jwtUtil.isValid(tamperedToken));
    }

    @Test
    void shouldRejectMalformedToken() {
        assertFalse(jwtUtil.isValid("not.a.valid.jwt.token"));
        assertFalse(jwtUtil.isValid(""));
        assertFalse(jwtUtil.isValid(null));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Set negative expiration so token is expired immediately
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", VALID_BASE64_SECRET);
        ReflectionTestUtils.setField(expiredJwtUtil, "expirationMs", -1000L);
        ReflectionTestUtils.setField(expiredJwtUtil, "rememberMeExpirationMs", -1000L);
        expiredJwtUtil.init();

        String token = expiredJwtUtil.generateToken("expired@example.com", false);

        assertFalse(expiredJwtUtil.isValid(token));
    }

    @Test
    void shouldUseSameKeyConsistentlyForSigningAndValidation() {
        SecretKey key1 = jwtUtil.getSigningKey();
        SecretKey key2 = jwtUtil.getSigningKey();

        assertNotNull(key1);
        assertSame(key1, key2, "The signing key instance should be cached and reused consistently.");

        String token = jwtUtil.generateToken("consistent@example.com", false);
        assertTrue(jwtUtil.isValid(token));
        assertEquals("consistent@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void shouldSupportBase64UrlEncodedSecretWithUnderscore() {
        // 48 bytes encoded with Base64URL (may contain '-' and '_')
        byte[] raw = new byte[48];
        for (int i = 0; i < raw.length; i++) {
            raw[i] = (byte) (i + 1);
        }
        String base64UrlSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        JwtUtil urlJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(urlJwtUtil, "secret", base64UrlSecret);
        ReflectionTestUtils.setField(urlJwtUtil, "expirationMs", 3600000L);
        ReflectionTestUtils.setField(urlJwtUtil, "rememberMeExpirationMs", 86400000L);
        urlJwtUtil.init();

        String token = urlJwtUtil.generateToken("base64url@example.com", false);
        assertTrue(urlJwtUtil.isValid(token));
        assertEquals("base64url@example.com", urlJwtUtil.extractEmail(token));
    }

    @Test
    void shouldGracefullyFallbackForUtf8PlainTextSecretWithUnderscores() {
        // Plain string containing underscores that previously failed with DecodingException
        String plainTextSecret = "REPLACE_THIS_WITH_A_SECURE_KEY_MINIMUM_32_CHARS";

        JwtUtil plainJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(plainJwtUtil, "secret", plainTextSecret);
        ReflectionTestUtils.setField(plainJwtUtil, "expirationMs", 3600000L);
        ReflectionTestUtils.setField(plainJwtUtil, "rememberMeExpirationMs", 86400000L);

        // Should not throw DecodingException: Illegal base64 character: '_'
        assertDoesNotThrow(plainJwtUtil::init);

        String token = plainJwtUtil.generateToken("plain@example.com", false);
        assertTrue(plainJwtUtil.isValid(token));
        assertEquals("plain@example.com", plainJwtUtil.extractEmail(token));
    }

    @Test
    void shouldThrowWhenSecretIsTooShort() {
        JwtUtil weakJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(weakJwtUtil, "secret", "too-short");
        ReflectionTestUtils.setField(weakJwtUtil, "expirationMs", 3600000L);
        ReflectionTestUtils.setField(weakJwtUtil, "rememberMeExpirationMs", 86400000L);

        assertThrows(WeakKeyException.class, weakJwtUtil::init);
    }

    @Test
    void shouldThrowWhenSecretIsBlankOrNull() {
        JwtUtil nullJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(nullJwtUtil, "secret", "   ");

        assertThrows(IllegalStateException.class, nullJwtUtil::init);
    }
}

