package com.agrodoc.service;

import com.agrodoc.dto.AuthResponse;
import com.agrodoc.dto.LoginRequest;
import com.agrodoc.dto.RegisterRequest;
import com.agrodoc.entity.User;
import com.agrodoc.repository.UserRepository;
import com.agrodoc.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — handles registration, login, and JWT generation.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register a new user account.
     *
     * @param request Registration form data (fullName, email, password)
     * @throws IllegalArgumentException if email is already registered
     */
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);

        userRepository.save(user);
    }

    /**
     * Authenticate a user and return a JWT token.
     *
     * @param request Login credentials (email, password, rememberMe)
     * @return AuthResponse containing the JWT token and basic user profile
     * @throws BadCredentialsException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        if (!user.isActive()) {
            throw new BadCredentialsException("This account has been deactivated. Please contact support.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), request.rememberMe());

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );

        return new AuthResponse(token, userDto);
    }
}
