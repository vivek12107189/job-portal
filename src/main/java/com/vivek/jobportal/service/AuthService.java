package com.vivek.jobportal.service;

import com.vivek.jobportal.config.JwtProperties;
import com.vivek.jobportal.dto.AuthResponse;
import com.vivek.jobportal.dto.LoginRequest;
import com.vivek.jobportal.dto.RefreshTokenRequest;
import com.vivek.jobportal.dto.RegisterRequest;
import com.vivek.jobportal.entity.RefreshToken;
import com.vivek.jobportal.entity.Role;
import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.BadRequestException;
import com.vivek.jobportal.repository.RefreshTokenRepository;
import com.vivek.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

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

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()->new BadRequestException("Invalid email or password"));



        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new BadRequestException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getRole().name());

        saveRefreshToken(user, refreshToken);
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtProperties.expirationMs());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String tokenHash = hashToken(token);

        if (!jwtService.validateToken(token) || !jwtService.isRefreshToken(token)) {
            throw new BadRequestException("Invalid refresh token");
        }

        RefreshToken savedToken = refreshTokenRepository.findByToken(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (savedToken.isRevoked() || savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        User user = savedToken.getUser();
        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);

        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getRole().name());
        saveRefreshToken(user, newRefreshToken);

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", jwtProperties.expirationMs());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(hashToken(request.getRefreshToken())).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(hashToken(token))
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.refreshExpirationMs() / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}


