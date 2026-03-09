package com.vivek.jobportal.service;

import com.vivek.jobportal.dto.LoginRequest;
import com.vivek.jobportal.dto.RegisterRequest;
import com.vivek.jobportal.entity.Role;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Role role = request.getRole() == null ? Role.JOB_SEEKER : request.getRole();

        if (role == Role.ADMIN) {
            throw new BadRequestException("Admin registration is not allowed");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new BadRequestException("Invalid email or password"));



        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new BadRequestException("Invalid email or password");
        }

        return jwtService.generateToken(user.getEmail(),user.getRole().name());
    }
}


