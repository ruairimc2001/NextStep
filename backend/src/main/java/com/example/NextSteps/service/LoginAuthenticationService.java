package com.example.NextSteps.service;

import com.example.NextSteps.dto.LoginRequest;
import com.example.NextSteps.dto.LoginResponse;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.UserRepository;
import com.example.NextSteps.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginAuthenticationService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginAuthenticationService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest loginRequest) {

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getUsername());

        if (userOptional.isEmpty()) {
            return new LoginResponse(false, "User not found", null, null, null);
        }

        User user = userOptional.get();

        if (!verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
            return new LoginResponse(false, "Invalid password", null, null, null);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new LoginResponse(true, "Login successful", user.getId(), user.getEmail(), token);
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // Check if the stored password is already hashed (starts with $2a$ for BCrypt)
        if (hashedPassword.startsWith("$2a$") || hashedPassword.startsWith("$2b$")) {
            return passwordEncoder.matches(rawPassword, hashedPassword);
        }
        // Fallback for plain text passwords (for existing data - should be migrated)
        return rawPassword.equals(hashedPassword);
    }
}

