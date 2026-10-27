package com.agrodoc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests to verify Spring Boot context, static resource routing, and security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgroDocApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies Spring application context boots cleanly
    }

    @Test
    void shouldServeCustomLoginPageAtLoginHtml() throws Exception {
        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome back")))
                .andExpect(content().string(not(containsString("Please sign in"))));
    }

    @Test
    void shouldRouteLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login.html"));
    }

    @Test
    void shouldRouteRootToIndexHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void shouldProtectApiEndpointsWithoutRedirectingToDefaultLoginPage() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldServeScanHtml() throws Exception {
        mockMvc.perform(get("/scan.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Upload Tomato Leaf Image")));
    }

    @Test
    void shouldServeResultHtml() throws Exception {
        mockMvc.perform(get("/result.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Diagnosis & Treatment Plan")));
    }

    @Test
    void shouldServeRegisterHtml() throws Exception {
        mockMvc.perform(get("/register.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create your account")));
    }

    @Test
    void shouldServeFrontendJsAssets() throws Exception {
        mockMvc.perform(get("/js/scan.js"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/js/result.js"))
                .andExpect(status().isOk());
    }
}
