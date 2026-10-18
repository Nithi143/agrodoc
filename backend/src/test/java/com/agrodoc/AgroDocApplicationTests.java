package com.agrodoc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify the Spring Boot application context loads successfully.
 *
 * Uses the 'test' profile which connects to an H2 in-memory database
 * instead of requiring a live MySQL connection.
 */
@SpringBootTest
@ActiveProfiles("test")
class AgroDocApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context started without errors.
        // No assertions needed — context load failure will throw an exception.
    }
}
