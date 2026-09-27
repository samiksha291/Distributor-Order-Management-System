package com.distributor.ordermanagement.controller;

import com.distributor.ordermanagement.dto.LoginRequest;
import com.distributor.ordermanagement.dto.LoginResponse;
import com.distributor.ordermanagement.dto.RegisterRequest;
import com.distributor.ordermanagement.entity.User;
import com.distributor.ordermanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.distributor.ordermanagement.dto.RegisterResponse;
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}