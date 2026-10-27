package com.agrodoc.config;

import com.agrodoc.security.JwtAuthFilter;
import com.agrodoc.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * AgroDoc Security Configuration
 *
 * Implements a stateless security architecture for AgroDoc's decoupled frontend.
 * - Disables default generated login page and HTTP Basic authentication.
 * - Configures CORS for frontend clients (http://localhost:5500, http://127.0.0.1:5500,
 *   and http://localhost:8080 for same-origin serving).
 * - Permits public access to auth endpoints and static resources.
 * - Keeps all protected /api/** endpoints secured behind JWT.
 * - Registers JwtAuthFilter before Spring Security's UsernamePasswordAuthenticationFilter.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${agrodoc.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500,http://localhost:8080}")
    private String allowedOrigins;

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsServiceImpl userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS and disable CSRF (stateless REST APIs using JWT)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)

            // Disable default generated login page and HTTP Basic
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            // Stateless session management (no HTTP session cookies)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Endpoint authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public authentication and registration endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Public disease library endpoints (read-only, no auth required)
                .requestMatchers("/api/diseases", "/api/diseases/**").permitAll()

                // Public access to uploaded scan preview images
                .requestMatchers("/api/scan/images/**").permitAll()

                // Static resources and public HTML pages served by Spring Boot
                .requestMatchers("/", "/login", "/index.html", "/login.html", "/register.html",
                                 "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                // All other /api/** endpoints require a valid JWT
                .requestMatchers("/api/**").authenticated()

                // Allow any other non-API requests (e.g. HTML pages, error handling)
                .anyRequest().permitAll()
            )

            // Register JWT filter before the default username/password filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
