package com.vivek.jobportal.controller;

import com.vivek.jobportal.dto.LoginRequest;
import com.vivek.jobportal.dto.RegisterRequest;
import com.vivek.jobportal.service.AuthService;
import com.vivek.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping ("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public  ResponseEntity<Map<String,String>> login(@RequestBody @Valid LoginRequest request){
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of("token",token));
    }


}

