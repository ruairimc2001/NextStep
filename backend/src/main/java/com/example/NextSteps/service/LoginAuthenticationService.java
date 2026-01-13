package com.example.NextSteps.service;

import com.example.NextSteps.dto.LoginRequest;
import com.example.NextSteps.dto.LoginResponse;
import com.example.NextSteps.entities.User;
import com.example.NextSteps.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginAuthenticationService {

    private final UserRepository userRepository;

    public LoginAuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest loginRequest) {

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getUsername());

        //TODO: Implement proper error handling
        if (userOptional.isEmpty()) {
            return new LoginResponse(false, "User not found", null, null);
        }

        User user = userOptional.get();

        // TODO: Implement proper password hashing verification
        if (!verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
            return new LoginResponse(false, "Invalid password", null, null);
        }

        return new LoginResponse(true, "Login successful", user.getId(), user.getEmail());
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // TODO: Implement proper password verification with BCrypt
        return rawPassword.equals(hashedPassword);
    }
}

