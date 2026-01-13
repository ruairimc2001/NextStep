package com.example.NextSteps.controller;

import com.example.NextSteps.dto.LoginRequest;
import com.example.NextSteps.dto.LoginResponse;
import com.example.NextSteps.service.LoginAuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//TODO: Look at these endpoints, see if they need to be changed, understand what CrossOrigin really is
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final LoginAuthenticationService loginAuthenticationService;

    public AuthController(LoginAuthenticationService loginAuthenticationService) {
        this.loginAuthenticationService = loginAuthenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = loginAuthenticationService.login(loginRequest);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }
}

